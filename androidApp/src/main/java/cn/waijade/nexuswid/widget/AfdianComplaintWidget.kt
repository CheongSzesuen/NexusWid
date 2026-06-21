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
import cn.waijade.nexuswid.data.afdian.AfdianPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AfdianComplaintWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = AfdianPreferences(context)
        val isDark = resolveIsDark(context)
        val (complaintCount, errorMsg) = loadComplaintCount(context)
        provideContent {
            AfdianComplaintContent(complaintCount, errorMsg, isDark, prefs.isConfigured)
        }
    }

    private suspend fun loadComplaintCount(context: Context): Pair<Int?, String?> {
        val prefs = AfdianPreferences(context)
        if (!prefs.isConfigured) {
            Log.d(TAG, "Not configured")
            return null to null
        }
        val json = Json { ignoreUnknownKeys = true }
        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
        }
        val service = AfdianApiService(httpClient, json)
        return try {
            val count = service.getComplaintCount(prefs.cookie)
            Log.d(TAG, "Complaint count: $count")
            count to null
        } catch (e: Exception) {
            Log.e(TAG, "Failed: ${e.message}")
            null to (e.message ?: "未知错误")
        } finally {
            httpClient.close()
        }
    }

    companion object {
        private const val TAG = "AfdianComplaintWidget"

        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(AfdianComplaintWidget::class.java).forEach { id ->
                AfdianComplaintWidget().update(context, id)
            }
        }
    }
}

class RefreshAfdianComplaintAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AfdianComplaintWidget().update(context, glanceId)
    }
}

@Composable
private fun AfdianComplaintContent(complaintCount: Int?, errorMsg: String?, isDark: Boolean, isConfigured: Boolean) {
    val size = LocalSize.current
    val edge: Dp = size.width.coerceAtMost(size.height)
    val pad = 14.dp
    val contentEdge = edge - pad * 2

    val displayText = if (!isConfigured) {
        "未配置"
    } else if (complaintCount != null) {
        formatCount(complaintCount)
    } else if (!errorMsg.isNullOrBlank()) {
        errorMsg.take(6)
    } else {
        "加载中"
    }

    val iconSize: Dp = (contentEdge * 0.15f).coerceIn(12.dp, 24.dp)
    val labelSize: Dp = (contentEdge * 0.12f).coerceIn(8.dp, 14.dp)

    val maxByHeight: Dp = contentEdge * 0.35f
    val cjkW = if (displayText.any { it in '\u4e00'..'\u9fff' }) 1.0f else 0.65f
    val maxByWidth: Dp = contentEdge / (displayText.length * cjkW + 0.5f)
    val clampedFontSize: Dp = maxByHeight.coerceAtMost(maxByWidth).coerceIn(10.dp, 48.dp)

    val bgColor = if (isDark) ComposeColor(0xFF0D1117) else ComposeColor(0xFFF6F8FA)
    val labelColor = if (isDark) ComposeColor.White.copy(alpha = 0.7f) else ComposeColor(0xFF656D76)
    val accentColor = ComposeColor(0xFF03D3EE)
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
                .clickable(onClick = actionRunCallback<RefreshAfdianComplaintAction>())
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
                        text = "未读投诉",
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
                        text = displayText,
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontSize = clampedFontSize.value.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun resolveIsDark(context: Context): Boolean {
    val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightMask == Configuration.UI_MODE_NIGHT_YES
}

private fun formatCount(count: Int): String {
    return when {
        count >= 10_000 -> "${count / 10_000}w"
        count >= 1_000 -> "${count / 1_000}k"
        else -> count.toString()
    }
}

class AfdianComplaintWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AfdianComplaintWidget()
}
