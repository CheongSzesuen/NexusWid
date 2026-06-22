package cn.waijade.nexuswid.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.glance.BitmapImageProvider
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
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
import cn.waijade.nexuswid.data.afdian.AfdianPreferences
import cn.waijade.nexuswid.data.afdian.AfdianTopSponsor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TopSponsorsDisplayData(
    val sponsors: List<AfdianTopSponsor>,
    val avatarBitmaps: Map<String, Bitmap> = emptyMap()
)

class AfdianTopSponsorsWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isDark = resolveIsDark(context)
        val prefs = AfdianPreferences(context)
        val (data, errorMsg) = if (!prefs.isConfigured) {
            null to "请先配置爱发电Cookie"
        } else {
            val userId = getUserIdFromCookie(prefs.cookie) ?: prefs.userId
            if (userId.isBlank()) {
                null to "获取用户信息失败"
            } else {
                // 缓存userId
                if (prefs.userId.isBlank() && userId.isNotBlank()) {
                    prefs.userId = userId
                }
                loadSponsors(context, userId)
            }
        }
        provideContent {
            AfdianTopSponsorsContent(data, errorMsg, isDark)
        }
    }

    private suspend fun getUserIdFromCookie(cookie: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val httpClient = HttpClient(OkHttp)
                val service = AfdianApiService(httpClient, kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
                service.getUserIdFromProfile(cookie)
            } catch (e: Exception) {
                Log.e(TAG, "getUserIdFromCookie err: ${e.message}")
                null
            }
        }
    }

    private suspend fun loadSponsors(context: Context, userId: String): Pair<TopSponsorsDisplayData?, String?> {
        return try {
            val sponsors = fetchTopSponsors(userId)
            if (sponsors.isNotEmpty()) {
                Log.d(TAG, "Loaded ${sponsors.size} sponsors")
                val bitmaps = mutableMapOf<String, Bitmap>()
                for (sponsor in sponsors.take(5)) {
                    val bitmap = downloadAvatar(context, sponsor.avatar)
                    if (bitmap != null) {
                        bitmaps[sponsor.userId] = bitmap
                    }
                }
                TopSponsorsDisplayData(sponsors, bitmaps) to null
            } else {
                null to "暂无赞助者"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed: ${e.message}")
            null to e.message
        }
    }

    private suspend fun fetchTopSponsors(userId: String): List<AfdianTopSponsor> {
        return withContext(Dispatchers.IO) {
            try {
                val httpClient = HttpClient(OkHttp)
                val service = AfdianApiService(httpClient, kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
                service.getTopSponsors(userId)
            } catch (e: Exception) {
                Log.e(TAG, "fetchTopSponsors err: ${e.message}")
                emptyList()
            }
        }
    }

    private suspend fun downloadAvatar(context: Context, avatarUrl: String): Bitmap? {
        if (avatarUrl.isBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = okhttp3.Request.Builder().url(avatarUrl).build()
                val response = client.newCall(request).execute()
                val bytes = response.body?.bytes()
                response.close()
                client.dispatcher.executorService.shutdown()
                if (bytes == null || bytes.isEmpty()) return@withContext null
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                Log.e(TAG, "Avatar download failed: ${e.message}")
                null
            }
        }
    }

    companion object {
        private const val TAG = "AfdianTopSponsors"

        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(AfdianTopSponsorsWidget::class.java).forEach { id ->
                AfdianTopSponsorsWidget().update(context, id)
            }
        }
    }
}

class RefreshAfdianTopSponsorsAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AfdianTopSponsorsWidget().update(context, glanceId)
    }
}

class OpenSponsorProfileAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val url = parameters[sponsorUrlParam] ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

private val sponsorUrlParam = ActionParameters.Key<String>("sponsor_url")

@Composable
private fun AfdianTopSponsorsContent(data: TopSponsorsDisplayData?, errorMsg: String?, isDark: Boolean) {
    val bgColor = if (isDark) ComposeColor(0xFF0D1117) else ComposeColor(0xFFF6F8FA)
    val headerTextColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val labelColor = if (isDark) ComposeColor.White.copy(alpha = 0.7f) else ComposeColor(0xFF656D76)
    val accentColor = ComposeColor(0xFF946CE6)
    val iconRes = if (isDark) R.drawable.ic_afdian_dark else R.drawable.ic_afdian

    val statusText = when {
        data == null && !errorMsg.isNullOrBlank() -> errorMsg
        data == null -> "加载中"
        else -> ""
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable(
                onClick = actionRunCallback<RefreshAfdianTopSponsorsAction>()
            )
    ) {
        if (statusText.isNotBlank()) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = null,
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    text = statusText,
                    style = TextStyle(
                        color = ColorProvider(labelColor),
                        fontSize = 12.sp
                    )
                )
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = null,
                        modifier = GlanceModifier.size(16.dp)
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = "赞助月榜",
                        style = TextStyle(
                            color = ColorProvider(headerTextColor),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    data!!.sponsors.take(3).forEachIndexed { index, sponsor ->
                        val rank = index + 1
                        val sponsorUrl = sponsor.urlSlug.takeIf { it.isNotBlank() }?.let { "https://ifdian.net/@$it" }

                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .clickable(
                                    onClick = actionRunCallback<OpenSponsorProfileAction>(
                                        actionParametersOf(sponsorUrlParam to (sponsorUrl ?: ""))
                                    )
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val bitmap = data.avatarBitmaps[sponsor.userId]
                            if (bitmap != null) {
                                Image(
                                    provider = BitmapImageProvider(bitmap),
                                    contentDescription = null,
                                    modifier = GlanceModifier
                                        .size(40.dp)
                                        .cornerRadius(20.dp)
                                )
                            } else {
                                Box(
                                    modifier = GlanceModifier
                                        .size(40.dp)
                                        .cornerRadius(20.dp)
                                        .background(ComposeColor(0xFFD0D7DE))
                                ) {}
                            }

                            Spacer(GlanceModifier.height(4.dp))

                            Text(
                                text = "$rank ${sponsor.name}",
                                style = TextStyle(
                                    color = ColorProvider(headerTextColor),
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.defaultWeight())
            }
        }
    }
}

private fun resolveIsDark(context: Context): Boolean {
    val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightMask == Configuration.UI_MODE_NIGHT_YES
}

class AfdianTopSponsorsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AfdianTopSponsorsWidget()
}
