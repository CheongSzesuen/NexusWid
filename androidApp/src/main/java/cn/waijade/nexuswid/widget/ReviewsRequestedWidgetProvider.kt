package cn.waijade.nexuswid.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import cn.waijade.nexuswid.R
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

private const val TAG = "ReviewsRequestedWidget"
private const val WIDGET_CORNER_RADIUS_DP = 36f
private const val ROOT_PADDING_DP = 20f
private const val MAX_BITMAP_EDGE_PX = 900f
private const val ACTION_REFRESH = "cn.waijade.nexuswid.ACTION_REFRESH_REVIEWS_REQUESTED"

class ReviewsRequestedWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called with ${appWidgetIds.size} widgets")
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_CONFIGURATION_CHANGED -> updateAll(context)
            ACTION_REFRESH -> {
                Log.d(TAG, "Refresh action received")
                updateAll(context)
            }
        }
    }

    companion object {

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ReviewsRequestedWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            appWidgetIds.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun fetchReviewRequestedCount(context: Context): Int {
            val prefs = GitHubPreferences(context)
            val token = prefs.token.takeIf { it.isNotBlank() } ?: return -1
            val pullRequestTypes = prefs.selectedPullRequestTypes

            return runBlocking(Dispatchers.IO) {
                runCatching {
                    val json = Json { ignoreUnknownKeys = true }
                    val httpClient = HttpClient(OkHttp) {
                        install(ContentNegotiation) {
                            json(json)
                        }
                    }
                    val apiService = GitHubApiService(httpClient, json)
                    apiService.getPullRequestCount(token, pullRequestTypes).getOrThrow()
                }.getOrElse {
                    Log.e(TAG, "fetchReviewRequestedCount: error", it)
                    -1
                }
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 250)
            val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWidth)
            val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHeight)

            val widthDp = maxWidth.coerceAtLeast(250).toFloat()
            val heightDp = maxHeight.coerceAtLeast(250).toFloat()

            val actualCount = fetchReviewRequestedCount(context)
            val prefs = GitHubPreferences(context)
            val countTextScale = prefs.debugCountTextScale
            val debugCountValue = prefs.debugCountValue
            val count = if (debugCountValue >= 0) debugCountValue else actualCount

            val bitmap = renderWidgetBitmap(
                context = context,
                widthDp = widthDp,
                heightDp = heightDp,
                count = count,
                countTextScale = countTextScale
            )

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_reviews_requested)
            remoteViews.setImageViewBitmap(R.id.widget_reviews_image, bitmap)

            // 设置点击刷新
            val refreshIntent = Intent(context, ReviewsRequestedWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            remoteViews.setOnClickPendingIntent(R.id.widget_root, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

        private fun renderWidgetBitmap(
            context: Context,
            widthDp: Float,
            heightDp: Float,
            count: Int,
            countTextScale: Float = 1.0f
        ): Bitmap {
            val rawDensity = context.resources.displayMetrics.density
            val rawBitmapWidth = (widthDp * rawDensity).coerceAtLeast(1f)
            val rawBitmapHeight = (heightDp * rawDensity).coerceAtLeast(1f)
            val edgeScale = minOf(
                1f,
                MAX_BITMAP_EDGE_PX / rawBitmapWidth,
                MAX_BITMAP_EDGE_PX / rawBitmapHeight
            )
            val density = rawDensity * edgeScale
            val bitmapWidth = (widthDp * density).roundToInt().coerceAtLeast(1)
            val bitmapHeight = (heightDp * density).roundToInt().coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val bgRadius = WIDGET_CORNER_RADIUS_DP * density
            val padding = 24f * density

            // Black background
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            val bgRect = RectF(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
            canvas.drawRoundRect(bgRect, bgRadius, bgRadius, bgPaint)

            val contentHeight = bitmapHeight - padding * 2
            val contentWidth = bitmapWidth - padding * 2

            // Icon - draw vector drawable
            val iconSize = (contentHeight * 0.15f).coerceAtMost(contentWidth * 0.3f)
            val iconX = padding
            val iconY = padding
            val iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_git_pull_request)
            if (iconDrawable != null) {
                val left = iconX.toInt()
                val top = iconY.toInt()
                iconDrawable.setBounds(left, top, left + iconSize.toInt(), top + iconSize.toInt())
                iconDrawable.draw(canvas)
            }

            // Labels - tight together at bottom
            val labelSize = (contentHeight * 0.16f).coerceAtMost(22f * density)
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(180, 255, 255, 255)
                textSize = labelSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
            }
            val requestedY = bitmapHeight - padding - labelSize * 0.2f
            val reviewsY = requestedY - labelSize * 1.3f

            // Count number - vertically centered between icon bottom and labels top
            val countText = if (count >= 0) formatCount(count) else "_"
            val availableWidth = contentWidth - padding * 2
            val maxCountTextSize = (contentHeight * 0.5f).coerceAtMost(availableWidth * 0.6f) * countTextScale
            
            // 动态调整字体大小以适应可用宽度
            val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }
            
            var countTextSize = maxCountTextSize
            countPaint.textSize = countTextSize
            var textWidth = countPaint.measureText(countText)
            
            // 如果文本宽度超过可用宽度，逐步缩小字体
            while (textWidth > availableWidth && countTextSize > 8f * density) {
                countTextSize *= 0.95f
                countPaint.textSize = countTextSize
                textWidth = countPaint.measureText(countText)
            }
            
            // Compensate for monospace left side bearing
            val textBounds = android.graphics.Rect()
            countPaint.getTextBounds(countText, 0, countText.length, textBounds)
            val countLeftX = padding - textBounds.left
            // Center between icon bottom and labels top
            val spaceTop = iconY + iconSize
            val spaceBottom = reviewsY - labelSize * 0.3f
            val spaceCenter = (spaceTop + spaceBottom) / 2f
            val countY = spaceCenter + countTextSize * 0.35f
            canvas.drawText(countText, countLeftX, countY, countPaint)

            canvas.drawText("Requested", padding, requestedY, labelPaint)
            canvas.drawText("Reviews", padding, reviewsY, labelPaint)

            return bitmap
        }

        private fun formatCount(count: Int): String {
            return when {
                count >= 1_000_000 -> "${count / 1_000_000}M+"
                count >= 1_000 -> "${count / 1_000}k+"
                else -> count.toString()
            }
        }
    }
}
