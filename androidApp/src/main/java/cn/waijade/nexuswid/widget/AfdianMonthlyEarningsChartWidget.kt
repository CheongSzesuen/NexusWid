package cn.waijade.nexuswid.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import cn.waijade.nexuswid.R
import cn.waijade.nexuswid.data.afdian.AfdianApiService
import cn.waijade.nexuswid.data.afdian.AfdianMonthlyIncome
import cn.waijade.nexuswid.data.afdian.AfdianPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.math.roundToInt

class AfdianMonthlyEarningsChartWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val (incomes, errorMsg) = loadMonthlyIncome(context)
        val isDark = resolveIsDark(context)
        val prefs = AfdianPreferences(context)
        provideContent {
            AfdianMonthlyEarningsChartContent(incomes, errorMsg, isDark, prefs.isConfigured)
        }
    }

    private suspend fun loadMonthlyIncome(context: Context): Pair<List<AfdianMonthlyIncome>?, String?> {
        val prefs = AfdianPreferences(context)
        if (!prefs.isConfigured) {
            Log.d(TAG, "Not configured")
            return null to null
        }

        val json = Json { ignoreUnknownKeys = true }
        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
        }
        return try {
            val service = AfdianApiService(httpClient, json)
            val incomes = service.getMonthlyIncome(prefs.cookie)
            Log.d(TAG, "Loaded ${incomes.size} monthly incomes")
            incomes to null
        } catch (e: Exception) {
            Log.e(TAG, "Failed: ${e.message}")
            null to (e.message ?: "未知错误")
        } finally {
            httpClient.close()
        }
    }

    companion object {
        private const val TAG = "AfdianMonthlyChart"

        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(AfdianMonthlyEarningsChartWidget::class.java).forEach { id ->
                AfdianMonthlyEarningsChartWidget().update(context, id)
            }
        }
    }
}

class RefreshAfdianMonthlyEarningsChartAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AfdianMonthlyEarningsChartWidget().update(context, glanceId)
    }
}

@Composable
private fun AfdianMonthlyEarningsChartContent(
    incomes: List<AfdianMonthlyIncome>?,
    errorMsg: String?,
    isDark: Boolean,
    isConfigured: Boolean
) {
    val size = LocalSize.current
    val pad = 14.dp

    val bgColor = if (isDark) ComposeColor(0xFF0D1117) else ComposeColor(0xFFF6F8FA)
    val textColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val labelColor = if (isDark) ComposeColor.White.copy(alpha = 0.7f) else ComposeColor(0xFF656D76)
    val accentColor = ComposeColor(0xFF946CE6)

    val iconRes = if (isDark) R.drawable.ic_afdian_dark else R.drawable.ic_afdian

    val statusText = when {
        !isConfigured -> "未配置"
        incomes == null && errorMsg != null -> errorMsg.take(8)
        incomes == null -> "加载中"
        incomes.isEmpty() -> "暂无数据"
        else -> null
    }

    val totalAmount = incomes?.sumOf { it.creatorAmount }
    val totalText = if (totalAmount != null) formatAmount(totalAmount) else ""

    val iconSize: Dp = 18.dp
    val labelSize: Dp = 12.dp

    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(28.dp)
                .background(bgColor)
                .padding(pad)
                .clickable(onClick = actionRunCallback<RefreshAfdianMonthlyEarningsChartAction>())
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = null,
                        modifier = GlanceModifier.size(iconSize)
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = "每月汇总",
                        style = TextStyle(
                            color = ColorProvider(labelColor),
                            fontSize = labelSize.value.sp
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    if (totalText.isNotBlank()) {
                        Text(
                            text = "¥$totalText",
                            style = TextStyle(
                                color = ColorProvider(accentColor),
                                fontSize = labelSize.value.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(GlanceModifier.height(6.dp))
                if (statusText != null) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusText,
                            style = TextStyle(
                                color = ColorProvider(labelColor),
                                fontSize = labelSize.value.sp
                            )
                        )
                    }
                } else if (incomes != null) {
                    val chartWidth = size.width - pad * 2
                    val chartHeight = size.height - pad * 2 - iconSize - 6.dp
                    if (chartWidth.value > 0f && chartHeight.value > 0f) {
                        val bitmap = renderMonthlyLineChartBitmap(
                            context = androidx.glance.LocalContext.current,
                            incomes = incomes,
                            widthDp = chartWidth.value,
                            heightDp = chartHeight.value,
                            isDark = isDark,
                            accentColor = ComposeColor(0xFF946CE6),
                            bgColor = bgColor,
                            textColor = textColor,
                            labelColor = labelColor
                        )
                        Image(
                            provider = ImageProvider(bitmap),
                            contentDescription = null,
                            modifier = GlanceModifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

private fun renderMonthlyLineChartBitmap(
    context: Context,
    incomes: List<AfdianMonthlyIncome>,
    widthDp: Float,
    heightDp: Float,
    isDark: Boolean,
    accentColor: ComposeColor,
    bgColor: ComposeColor,
    textColor: ComposeColor,
    labelColor: ComposeColor
): Bitmap {
    val density = context.resources.displayMetrics.density
    val bitmapWidth = (widthDp * density).roundToInt().coerceAtLeast(1)
    val bitmapHeight = (heightDp * density).roundToInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 7f * density
        color = if (isDark) android.graphics.Color.argb(140, 255, 255, 255)
        else android.graphics.Color.argb(140, 100, 109, 118)
        textAlign = Paint.Align.RIGHT
    }
    val yLabelWidth = yLabelPaint.measureText("24.00") + 4f * density

    val paddingLeft = yLabelWidth + 4f * density
    val paddingRight = 6f * density
    val paddingTop = 4f * density
    val paddingBottom = 16f * density

    val chartLeft = paddingLeft
    val chartRight = bitmapWidth - paddingRight
    val chartTop = paddingTop
    val chartBottom = bitmapHeight - paddingBottom
    val chartWidth = chartRight - chartLeft
    val chartHeight = chartBottom - chartTop

    if (chartWidth <= 0f || chartHeight <= 0f) return bitmap

    val amounts = incomes.map { it.creatorAmount }
    val maxAmount = max(amounts.maxOrNull() ?: 0.0, 1.0)
    val months = incomes.map { "${it.month}月" }

    val pointCount = amounts.size
    if (pointCount < 2) return bitmap

    val stepX = chartWidth / (pointCount - 1).toFloat()

    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        color = android.graphics.Color.parseColor("#946CE6")
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = android.graphics.Color.parseColor("#946CE6")
    }

    val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 7f * density
        color = if (isDark) android.graphics.Color.argb(140, 255, 255, 255)
        else android.graphics.Color.argb(140, 100, 109, 118)
        textAlign = Paint.Align.CENTER
    }

    val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 0.5f * density
        color = if (isDark) android.graphics.Color.argb(25, 255, 255, 255)
        else android.graphics.Color.argb(25, 0, 0, 0)
    }

    val points = amounts.mapIndexed { index, amount ->
        val x = chartLeft + index * stepX
        val y = chartBottom - (amount / maxAmount * chartHeight).toFloat()
        x to y
    }

    val gridLines = 3
    for (i in 0..gridLines) {
        val y = chartTop + chartHeight * i / gridLines
        canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
        val amountValue = maxAmount * (gridLines - i) / gridLines
        val label = formatYLabel(amountValue)
        canvas.drawText(label, yLabelPaint.measureText(label), y + 3f * density, yLabelPaint)
    }

    val linePath = Path()
    linePath.moveTo(points[0].first, points[0].second)
    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]
        val midX = (prev.first + curr.first) / 2f
        linePath.cubicTo(midX, prev.second, midX, curr.second, curr.first, curr.second)
    }
    canvas.drawPath(linePath, linePaint)

    val fillPath = Path()
    fillPath.moveTo(points[0].first, chartBottom)
    fillPath.lineTo(points[0].first, points[0].second)
    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]
        val midX = (prev.first + curr.first) / 2f
        fillPath.cubicTo(midX, prev.second, midX, curr.second, curr.first, curr.second)
    }
    fillPath.lineTo(points.last().first, chartBottom)
    fillPath.close()

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        shader = LinearGradient(
            0f, chartTop, 0f, chartBottom,
            android.graphics.Color.parseColor("#4D946CE6"),
            android.graphics.Color.parseColor("#00946CE6"),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawPath(fillPath, fillPaint)

    val dotRadius = 2.5f * density
    points.forEachIndexed { index, (x, y) ->
        canvas.drawCircle(x, y, dotRadius, dotPaint)
    }

    val showEveryN = when {
        pointCount <= 6 -> 1
        pointCount <= 12 -> 2
        else -> 3
    }
    points.forEachIndexed { index, (x, _) ->
        if (index % showEveryN == 0 || index == points.size - 1) {
            val monthLabel = months[index]
            canvas.drawText(monthLabel, x, chartBottom + 11f * density, xLabelPaint)
        }
    }

    return bitmap
}

private fun formatYLabel(amount: Double): String {
    return when {
        amount >= 100 -> "%.0f".format(amount)
        amount >= 10 -> "%.1f".format(amount)
        else -> "%.1f".format(amount)
    }
}

private fun resolveIsDark(context: Context): Boolean {
    val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightMask == Configuration.UI_MODE_NIGHT_YES
}

private fun formatAmount(amount: Double): String {
    return when {
        amount >= 1_000_000 -> "${(amount / 1_000_000).let { if (it == it.toLong().toDouble()) "${it.toLong()}" else "%.1f".format(it) }}M"
        amount >= 10_000 -> "${(amount / 1_000).let { if (it == it.toLong().toDouble()) "${it.toLong()}" else "%.1f".format(it) }}k"
        else -> "%.2f".format(amount)
    }
}

class AfdianMonthlyEarningsChartWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AfdianMonthlyEarningsChartWidget()
}