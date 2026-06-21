package cn.waijade.nexuswid.widget

import android.content.Context
import android.content.res.Configuration
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
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import cn.waijade.nexuswid.R
import cn.waijade.nexuswid.data.afdian.AfdianApiService
import cn.waijade.nexuswid.data.afdian.AfdianEarnings
import cn.waijade.nexuswid.data.afdian.AfdianPreferences
import cn.waijade.nexuswid.data.afdian.AfdianResult
import cn.waijade.nexuswid.data.afdian.AfdianServiceConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AfdianMonthlyEarningsWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val (earnings, errorMsg) = loadEarnings(context)
        val prefs = AfdianPreferences(context)
        val isDark = resolveIsDark(context)
        provideContent {
            AfdianMonthlyEarningsContent(earnings, errorMsg, isDark, prefs.isConfigured)
        }
    }

    private suspend fun loadEarnings(context: Context): Pair<AfdianEarnings?, String?> {
        val prefs = AfdianPreferences(context)

        if (!prefs.isConfigured) {
            Log.d(TAG, "Not configured")
            return null to null
        }

        Log.d(TAG, "Cookie configured, length=${prefs.cookie.length}, starts with auth_token=: ${prefs.cookie.startsWith("auth_token=")}")

        val json = Json { ignoreUnknownKeys = true }
        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
        }
        val service = AfdianApiService(httpClient, json)
        return when (val result = service.getEarnings(AfdianServiceConfig(cookie = prefs.cookie))) {
            is AfdianResult.Success -> {
                Log.d(TAG, "Earnings loaded: totalAmount=${result.earnings.totalAmount}")
                result.earnings to null
            }
            is AfdianResult.Error -> {
                Log.e(TAG, "Failed: ${result.message}")
                null to result.message
            }
        }
    }

    companion object {
        private const val TAG = "AfdianMonthlyWidget"

        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(AfdianMonthlyEarningsWidget::class.java).forEach { id ->
                AfdianMonthlyEarningsWidget().update(context, id)
            }
        }
    }
}

class RefreshAfdianMonthlyEarningsAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AfdianMonthlyEarningsWidget().update(context, glanceId)
    }
}

@Composable
private fun AfdianMonthlyEarningsContent(earnings: AfdianEarnings?, errorMsg: String?, isDark: Boolean, isConfigured: Boolean) {
    val size = LocalSize.current
    val edge: Dp = size.width.coerceAtMost(size.height)
    val pad = 14.dp
    val contentEdge = edge - pad * 2

    val amountText = if (!isConfigured) {
        "未配置"
    } else if (earnings != null) {
        formatAmount(earnings.monthlyAmount)
    } else if (!errorMsg.isNullOrBlank()) {
        errorMsg.take(6)
    } else {
        "加载中"
    }

    val iconSize: Dp = (contentEdge * 0.15f).coerceIn(12.dp, 24.dp)
    val labelSize: Dp = (contentEdge * 0.12f).coerceIn(8.dp, 14.dp)

    val maxByHeight: Dp = contentEdge * 0.35f
    val cjkW = if (amountText.any { it in '\u4e00'..'\u9fff' }) 1.0f else 0.65f
    val maxByWidth: Dp = contentEdge / (amountText.length * cjkW + 0.5f)
    val clampedFontSize: Dp = maxByHeight.coerceAtMost(maxByWidth).coerceIn(10.dp, 48.dp)

    val bgColor = if (isDark) ComposeColor(0xFF0D1117) else ComposeColor(0xFFF6F8FA)
    val textColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val labelColor = if (isDark) ComposeColor.White.copy(alpha = 0.7f) else ComposeColor(0xFF656D76)
    val accentColor = ComposeColor(0xFF946CE6)
    val iconRes = if (isDark) R.drawable.ic_afdian_dark else R.drawable.ic_afdian

    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = GlanceModifier
                .size(edge)
                .cornerRadius(28.dp)
                .background(bgColor)
                .padding(pad)
                .clickable(onClick = actionRunCallback<RefreshAfdianMonthlyEarningsAction>())
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
                    Spacer(GlanceModifier.width(8.dp))
                    Text(
                        text = "爱发电",
                        style = TextStyle(
                            color = ColorProvider(labelColor),
                            fontSize = labelSize.value.sp
                        )
                    )
                }
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = amountText,
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontSize = clampedFontSize.value.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
                Text(
                    text = "本月收益",
                    style = TextStyle(
                        color = ColorProvider(labelColor),
                        fontSize = labelSize.value.sp
                    )
                )
            }
        }
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

class AfdianMonthlyEarningsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AfdianMonthlyEarningsWidget()
}