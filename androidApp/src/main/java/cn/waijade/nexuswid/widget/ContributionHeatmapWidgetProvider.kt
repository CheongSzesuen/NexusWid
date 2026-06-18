package cn.waijade.nexuswid.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import cn.waijade.nexuswid.R
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.HeatmapTheme
import cn.waijade.nexuswid.data.github.GitHubPreferences
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "ContributionHeatmapWidgetProvider"

private const val ROOT_PADDING_HORIZONTAL_DP = 6f
private const val ROOT_PADDING_VERTICAL_DP = 6f
private const val DEFAULT_WIDGET_SIZE_DP = 110
private const val CELL_RADIUS_RATIO = 0.24f
private const val CORNER_OUTER_RADIUS_RATIO = 0.5735f
private const val CELL_SIZE_SCALE = 1f
private const val WIDGET_CORNER_RADIUS_DP = 16f
private const val MAX_BITMAP_EDGE_PX = 900f
private const val MAX_BITMAP_AREA_PX = 900f * 900f
private const val SIZE_PREFS_NAME = "contribution_heatmap_widget_size"
private const val SIZE_KEY_WIDTH_PREFIX = "size_width_"
private const val SIZE_KEY_HEIGHT_PREFIX = "size_height_"
private const val SIZE_KEY_WIDTH_SOURCE_PREFIX = "size_width_source_"
private const val SIZE_KEY_HEIGHT_SOURCE_PREFIX = "size_height_source_"
private const val SIZE_KEY_RAW_MIN_WIDTH_PREFIX = "raw_min_width_"
private const val SIZE_KEY_RAW_MAX_WIDTH_PREFIX = "raw_max_width_"
private const val SIZE_KEY_RAW_MIN_HEIGHT_PREFIX = "raw_min_height_"
private const val SIZE_KEY_RAW_MAX_HEIGHT_PREFIX = "raw_max_height_"
private const val ACTION_UI_MODE_CHANGED = "android.intent.action.UI_MODE_CHANGED"
private val POST_ADD_REFRESH_DELAYS_MS = longArrayOf(450L, 1350L)

class ContributionHeatmapWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called with ${appWidgetIds.size} widgets")
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
            schedulePostAddStabilizeRefresh(context, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidget(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val prefs = context.getSharedPreferences(SIZE_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            appWidgetIds.forEach { id ->
                remove("$SIZE_KEY_WIDTH_PREFIX$id")
                remove("$SIZE_KEY_HEIGHT_PREFIX$id")
                remove("$SIZE_KEY_WIDTH_SOURCE_PREFIX$id")
                remove("$SIZE_KEY_HEIGHT_SOURCE_PREFIX$id")
                remove("$SIZE_KEY_RAW_MIN_WIDTH_PREFIX$id")
                remove("$SIZE_KEY_RAW_MAX_WIDTH_PREFIX$id")
                remove("$SIZE_KEY_RAW_MIN_HEIGHT_PREFIX$id")
                remove("$SIZE_KEY_RAW_MAX_HEIGHT_PREFIX$id")
            }
        }.apply()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_CONFIGURATION_CHANGED,
            ACTION_UI_MODE_CHANGED -> updateAll(context)
        }
    }

    companion object {

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ContributionHeatmapWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            appWidgetIds.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            optionsOverride: Bundle? = null
        ) {
            val options = if (optionsOverride == null) {
                appWidgetManager.getAppWidgetOptions(appWidgetId)
            } else {
                Bundle(appWidgetManager.getAppWidgetOptions(appWidgetId)).apply {
                    putAll(optionsOverride)
                }
            }
            val size = resolveWidgetSizeDp(context, appWidgetId, options, optionsOverride != null)
            val fullWidthDp = size.first
            val fullHeightDp = size.second
            val contentWidthDp = (fullWidthDp - ROOT_PADDING_HORIZONTAL_DP * 2f).coerceAtLeast(1f)
            val contentHeightDp = (fullHeightDp - ROOT_PADDING_VERTICAL_DP * 2f).coerceAtLeast(1f)

            val columns = HeatmapGridCalculator.calculateColumns(
                widthDp = contentWidthDp,
                heightDp = contentHeightDp
            )
            val cellSizeDp = HeatmapGridCalculator.calculateCellSize(
                widthDp = contentWidthDp,
                heightDp = contentHeightDp,
                columns = columns
            ) * CELL_SIZE_SCALE
            val widgetPrefs = GitHubPreferences(context)
            val allLevels = HeatmapWidgetDataStore(context).getGrid(
                columns = HeatmapGridCalculator.MAX_COLS,
                weekStartsOnMonday = widgetPrefs.weekStartsOnMonday
            )
            val prefs = GitHubPreferences(context)
            val isDark = resolveIsDark(context, prefs.widgetHeatmapColorMode)
            val theme = HeatmapTheme.resolveCurrentTheme()
            val heatmapPalette = theme.palette(isDark)
            val palette = heatmapPalette.toIntArray()

            val bitmap = renderHeatmapBitmap(
                context = context,
                fullWidthDp = fullWidthDp,
                fullHeightDp = fullHeightDp,
                contentWidthDp = contentWidthDp,
                contentHeightDp = contentHeightDp,
                columns = columns,
                cellSizeDp = cellSizeDp,
                allLevels = allLevels,
                palette = palette,
                bgColor = heatmapPalette.containerBg.toArgb(),
                borderColor = heatmapPalette.border.toArgb()
            )

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_contribution_heatmap)
            remoteViews.setImageViewBitmap(R.id.widget_heatmap_image, bitmap)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

        private fun resolveWidgetSizeDp(
            context: Context,
            appWidgetId: Int,
            options: Bundle?,
            shouldPersist: Boolean
        ): Pair<Float, Float> {
            val prefs = context.getSharedPreferences(SIZE_PREFS_NAME, Context.MODE_PRIVATE)
            val cachedWidth = prefs.getFloat("$SIZE_KEY_WIDTH_PREFIX$appWidgetId", -1f)
            val cachedHeight = prefs.getFloat("$SIZE_KEY_HEIGHT_PREFIX$appWidgetId", -1f)
            val hasSourceInfo = prefs.contains("$SIZE_KEY_WIDTH_SOURCE_PREFIX$appWidgetId") &&
                prefs.contains("$SIZE_KEY_HEIGHT_SOURCE_PREFIX$appWidgetId")
            if (!shouldPersist && cachedWidth > 0f && cachedHeight > 0f && hasSourceInfo) {
                return cachedWidth to cachedHeight
            }

            val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, -1) ?: -1
            val maxWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, -1) ?: -1
            val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, -1) ?: -1
            val maxHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, -1) ?: -1

            val previousMinWidth = prefs.getInt("$SIZE_KEY_RAW_MIN_WIDTH_PREFIX$appWidgetId", -1)
            val previousMaxWidth = prefs.getInt("$SIZE_KEY_RAW_MAX_WIDTH_PREFIX$appWidgetId", -1)
            val previousMinHeight = prefs.getInt("$SIZE_KEY_RAW_MIN_HEIGHT_PREFIX$appWidgetId", -1)
            val previousMaxHeight = prefs.getInt("$SIZE_KEY_RAW_MAX_HEIGHT_PREFIX$appWidgetId", -1)
            val previousWidthSource = prefs.getInt("$SIZE_KEY_WIDTH_SOURCE_PREFIX$appWidgetId", 0)
            val previousHeightSource = prefs.getInt("$SIZE_KEY_HEIGHT_SOURCE_PREFIX$appWidgetId", 0)
            val widthPick = pickCurrentAxisDp(
                firstRaw = minWidth,
                secondRaw = maxWidth,
                previousFirstRaw = previousMinWidth,
                previousSecondRaw = previousMaxWidth,
                previousSource = previousWidthSource,
                cached = cachedWidth,
                fallback = DEFAULT_WIDGET_SIZE_DP.toFloat(),
                preferLargerBound = false
            )
            val heightPick = pickCurrentAxisDp(
                firstRaw = minHeight,
                secondRaw = maxHeight,
                previousFirstRaw = previousMinHeight,
                previousSecondRaw = previousMaxHeight,
                previousSource = previousHeightSource,
                cached = cachedHeight,
                fallback = DEFAULT_WIDGET_SIZE_DP.toFloat(),
                preferLargerBound = true
            )
            val resolvedHeight = stabilizeHeightDp(
                cachedHeight = cachedHeight,
                pickedHeight = heightPick.value
            )
            val placeholderOptions = looksLikePlaceholderOptions(
                minWidth = minWidth,
                maxWidth = maxWidth,
                minHeight = minHeight,
                maxHeight = maxHeight
            )
            val shouldWriteCache = shouldPersist || !placeholderOptions || cachedWidth > 0f || cachedHeight > 0f

            if (shouldWriteCache) {
                prefs.edit()
                    .putFloat("$SIZE_KEY_WIDTH_PREFIX$appWidgetId", widthPick.value)
                    .putFloat("$SIZE_KEY_HEIGHT_PREFIX$appWidgetId", resolvedHeight)
                    .putInt("$SIZE_KEY_WIDTH_SOURCE_PREFIX$appWidgetId", widthPick.source)
                    .putInt(
                        "$SIZE_KEY_HEIGHT_SOURCE_PREFIX$appWidgetId",
                        if (resolvedHeight > heightPick.value + 1f) previousHeightSource else heightPick.source
                    )
                    .putInt("$SIZE_KEY_RAW_MIN_WIDTH_PREFIX$appWidgetId", minWidth)
                    .putInt("$SIZE_KEY_RAW_MAX_WIDTH_PREFIX$appWidgetId", maxWidth)
                    .putInt("$SIZE_KEY_RAW_MIN_HEIGHT_PREFIX$appWidgetId", minHeight)
                    .putInt("$SIZE_KEY_RAW_MAX_HEIGHT_PREFIX$appWidgetId", maxHeight)
                    .apply()
            }

            return widthPick.value to resolvedHeight
        }

        private fun stabilizeHeightDp(
            cachedHeight: Float,
            pickedHeight: Float
        ): Float {
            if (cachedHeight <= 0f) {
                return pickedHeight.coerceAtLeast(1f)
            }
            return max(cachedHeight, pickedHeight).coerceAtLeast(1f)
        }

        private fun looksLikePlaceholderOptions(
            minWidth: Int,
            maxWidth: Int,
            minHeight: Int,
            maxHeight: Int
        ): Boolean {
            val widthMin = if (minWidth > 0) minWidth else DEFAULT_WIDGET_SIZE_DP
            val widthMax = if (maxWidth > 0) maxWidth else widthMin
            val heightMin = if (minHeight > 0) minHeight else DEFAULT_WIDGET_SIZE_DP
            val heightMax = if (maxHeight > 0) maxHeight else heightMin
            return widthMin <= DEFAULT_WIDGET_SIZE_DP &&
                widthMax <= DEFAULT_WIDGET_SIZE_DP &&
                heightMin <= DEFAULT_WIDGET_SIZE_DP &&
                heightMax <= DEFAULT_WIDGET_SIZE_DP
        }

        private fun schedulePostAddStabilizeRefresh(context: Context, appWidgetId: Int) {
            val appContext = context.applicationContext
            val handler = Handler(Looper.getMainLooper())
            POST_ADD_REFRESH_DELAYS_MS.forEach { delayMs ->
                handler.postDelayed({
                    val appWidgetManager = AppWidgetManager.getInstance(appContext)
                    val componentName = ComponentName(appContext, ContributionHeatmapWidgetProvider::class.java)
                    val activeIds = appWidgetManager.getAppWidgetIds(componentName)
                    if (activeIds.contains(appWidgetId)) {
                        updateWidget(appContext, appWidgetManager, appWidgetId)
                    }
                }, delayMs)
            }
        }

        private fun pickCurrentAxisDp(
            firstRaw: Int,
            secondRaw: Int,
            previousFirstRaw: Int,
            previousSecondRaw: Int,
            previousSource: Int,
            cached: Float,
            fallback: Float,
            preferLargerBound: Boolean
        ): AxisPick {
            val first = firstRaw.takeIf { it > 0 }?.toFloat()
            val second = secondRaw.takeIf { it > 0 }?.toFloat()
            val candidates = listOfNotNull(first, second).distinct()
            if (candidates.isEmpty()) {
                return AxisPick(fallback.coerceAtLeast(1f), 0)
            }
            if (candidates.size == 1) {
                return if (first != null) {
                    AxisPick(first.coerceAtLeast(1f), 1)
                } else {
                    AxisPick(second!!.coerceAtLeast(1f), 2)
                }
            }
            if (preferLargerBound && first != null && second != null) {
                return if (second >= first) {
                    AxisPick(second.coerceAtLeast(1f), 2)
                } else {
                    AxisPick(first.coerceAtLeast(1f), 1)
                }
            }

            val firstChanged = previousFirstRaw > 0 && firstRaw != previousFirstRaw
            val secondChanged = previousSecondRaw > 0 && secondRaw != previousSecondRaw
            if (firstChanged.xor(secondChanged)) {
                return if (firstChanged) {
                    AxisPick(first?.coerceAtLeast(1f) ?: fallback.coerceAtLeast(1f), 1)
                } else {
                    AxisPick(second?.coerceAtLeast(1f) ?: fallback.coerceAtLeast(1f), 2)
                }
            }

            if (!firstChanged && !secondChanged && previousSource == 1 && first != null && second != null) {
                val nearFirst = cached > 0f && abs(cached - first) <= 1f
                val hasMeaningfulRange = (second - first) >= 8f
                if (nearFirst && hasMeaningfulRange) {
                    return AxisPick(second.coerceAtLeast(1f), 2)
                }
            }

            if (previousSource == 1 && first != null) {
                return AxisPick(first.coerceAtLeast(1f), 1)
            }
            if (previousSource == 2 && second != null) {
                return AxisPick(second.coerceAtLeast(1f), 2)
            }
            if (cached > 0f && !firstChanged && !secondChanged) {
                return AxisPick(cached.coerceAtLeast(1f), previousSource.coerceAtLeast(0))
            }
            if (cached > 0f && first != null && second != null) {
                val firstDistance = abs(first - cached)
                val secondDistance = abs(second - cached)
                if (firstDistance != secondDistance) {
                    return if (firstDistance < secondDistance) {
                        AxisPick(first.coerceAtLeast(1f), 1)
                    } else {
                        AxisPick(second.coerceAtLeast(1f), 2)
                    }
                }
            }
            return AxisPick(max(first ?: 0f, second ?: 0f).coerceAtLeast(1f), if (second != null) 2 else 1)
        }

        private data class AxisPick(
            val value: Float,
            val source: Int
        )

        private fun renderHeatmapBitmap(
            context: Context,
            fullWidthDp: Float,
            fullHeightDp: Float,
            contentWidthDp: Float,
            contentHeightDp: Float,
            columns: Int,
            cellSizeDp: Float,
            allLevels: List<Int>,
            palette: IntArray,
            bgColor: Int,
            borderColor: Int
        ): Bitmap {
            val rawDensity = context.resources.displayMetrics.density
            val rawBitmapWidth = (fullWidthDp * rawDensity).coerceAtLeast(1f)
            val rawBitmapHeight = (fullHeightDp * rawDensity).coerceAtLeast(1f)
            val edgeScale = minOf(
                1f,
                MAX_BITMAP_EDGE_PX / rawBitmapWidth,
                MAX_BITMAP_EDGE_PX / rawBitmapHeight
            )
            val areaScale = minOf(1f, kotlin.math.sqrt(MAX_BITMAP_AREA_PX / (rawBitmapWidth * rawBitmapHeight)))
            val renderScale = minOf(edgeScale, areaScale)
            val density = rawDensity * renderScale
            val bitmapWidth = (fullWidthDp * density).roundToInt().coerceAtLeast(1)
            val bitmapHeight = (fullHeightDp * density).roundToInt().coerceAtLeast(1)
            val contentWidthPx = (contentWidthDp * density).coerceAtLeast(1f)
            val contentHeightPx = (contentHeightDp * density).coerceAtLeast(1f)
            val paddingH = (ROOT_PADDING_HORIZONTAL_DP * density).coerceAtLeast(0f)
            val paddingV = (ROOT_PADDING_VERTICAL_DP * density).coerceAtLeast(0f)
            val baseGapPx = (HeatmapGridCalculator.GAP_DP * density).coerceAtLeast(0f)

            val rows = HeatmapGridCalculator.ROWS
            val maxCellByWidth = ((contentWidthPx - baseGapPx * (columns - 1)) / columns).coerceAtLeast(1f)
            val maxCellByHeight = ((contentHeightPx - baseGapPx * (rows - 1)) / rows).coerceAtLeast(1f)
            val cellSizePx = min((cellSizeDp * density).coerceAtLeast(1f), min(maxCellByWidth, maxCellByHeight))
            val horizontalGapPx = if (columns > 1) {
                ((contentWidthPx - cellSizePx * columns) / (columns - 1)).coerceAtLeast(baseGapPx)
            } else {
                0f
            }
            val verticalGapPx = if (rows > 1) {
                ((contentHeightPx - cellSizePx * rows) / (rows - 1)).coerceAtLeast(baseGapPx)
            } else {
                0f
            }
            val gridWidth = cellSizePx * columns + horizontalGapPx * (columns - 1)
            val gridHeight = cellSizePx * rows + verticalGapPx * (rows - 1)
            val gridStartX = paddingH + ((contentWidthPx - gridWidth) / 2f).coerceAtLeast(0f)
            val gridStartY = paddingV + ((contentHeightPx - gridHeight) / 2f).coerceAtLeast(0f)

            val totalLogicalColumns = (allLevels.size / rows).coerceAtLeast(1)
            val visualToLogicalColumns = buildVisibleLogicalColumns(
                visibleColumns = columns,
                totalLogicalColumns = totalLogicalColumns
            )
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = borderColor
                strokeWidth = density.coerceAtLeast(1f)
            }
            val path = Path()
            val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val bgRadius = WIDGET_CORNER_RADIUS_DP * density
            val smallRadius = cellSizePx * CELL_RADIUS_RATIO
            val largeRadius = cellSizePx * CORNER_OUTER_RADIUS_RATIO
            val lastRow = rows - 1

            val bgRect = RectF(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
            paint.color = bgColor
            canvas.drawRoundRect(bgRect, bgRadius, bgRadius, paint)
            canvas.drawRoundRect(bgRect, bgRadius, bgRadius, strokePaint)

            for (visualCol in 0 until columns) {
                val logicalCol = visualToLogicalColumns[visualCol]
                for (row in 0 until rows) {
                    val index = logicalCol * rows + row
                    val level = allLevels.getOrNull(index)?.coerceIn(0, 4) ?: 0
                    paint.color = palette[level]

                    val top = gridStartY + row * (cellSizePx + verticalGapPx)
                    val left = gridStartX + visualCol * (cellSizePx + horizontalGapPx)
                    val rect = RectF(left, top, left + cellSizePx, top + cellSizePx)
                    val cornerType = resolveCornerType(visualCol, row, columns, lastRow)

                    if (cornerType == null) {
                        canvas.drawRoundRect(rect, smallRadius, smallRadius, paint)
                    } else {
                        val radii = floatArrayOf(
                            smallRadius, smallRadius,
                            smallRadius, smallRadius,
                            smallRadius, smallRadius,
                            smallRadius, smallRadius
                        )
                        when (cornerType) {
                            CornerType.TOP_LEFT -> {
                                radii[0] = largeRadius
                                radii[1] = largeRadius
                            }

                            CornerType.TOP_RIGHT -> {
                                radii[2] = largeRadius
                                radii[3] = largeRadius
                            }

                            CornerType.BOTTOM_RIGHT -> {
                                radii[4] = largeRadius
                                radii[5] = largeRadius
                            }

                            CornerType.BOTTOM_LEFT -> {
                                radii[6] = largeRadius
                                radii[7] = largeRadius
                            }
                        }
                        path.reset()
                        path.addRoundRect(rect, radii, Path.Direction.CW)
                        canvas.drawPath(path, paint)
                    }
                }
            }
            return bitmap
        }

        private fun resolveCornerType(
            visualCol: Int,
            row: Int,
            visualColumns: Int,
            lastRow: Int
        ): CornerType? {
            if (visualColumns <= 0) return null
            if (visualColumns == 1) {
                return when (row) {
                    0 -> CornerType.TOP_LEFT
                    lastRow -> CornerType.BOTTOM_LEFT
                    else -> null
                }
            }
            val lastCol = visualColumns - 1
            return when {
                visualCol == 0 && row == 0 -> CornerType.TOP_LEFT
                visualCol == lastCol && row == 0 -> CornerType.TOP_RIGHT
                visualCol == lastCol && row == lastRow -> CornerType.BOTTOM_RIGHT
                visualCol == 0 && row == lastRow -> CornerType.BOTTOM_LEFT
                else -> null
            }
        }

        private fun buildVisibleLogicalColumns(
            visibleColumns: Int,
            totalLogicalColumns: Int
        ): List<Int> {
            val safeVisible = visibleColumns.coerceAtLeast(1)
            val safeTotal = totalLogicalColumns.coerceAtLeast(1)
            if (safeTotal == 1) return listOf(0)

            val actualVisible = safeVisible.coerceAtMost(safeTotal)
            val startColumn = (safeTotal - actualVisible).coerceAtLeast(0)
            return List(actualVisible) { index -> startColumn + index }
        }

        private fun resolveIsDark(context: Context, mode: HeatmapColorMode): Boolean {
            return when (mode) {
                HeatmapColorMode.LIGHT -> false
                HeatmapColorMode.DARK -> true
                HeatmapColorMode.SYSTEM -> isDarkMode(context)
            }
        }

        private fun isDarkMode(context: Context): Boolean {
            val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightMask == Configuration.UI_MODE_NIGHT_YES
        }
    }
}

private enum class CornerType {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT
}