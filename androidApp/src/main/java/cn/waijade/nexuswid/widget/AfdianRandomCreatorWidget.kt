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
import cn.waijade.nexuswid.data.afdian.AfdianCreatorListResponse
import cn.waijade.nexuswid.data.afdian.AfdianPreferences
import cn.waijade.nexuswid.data.afdian.AfdianRandomCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class RandomCreatorDisplayData(
    val creator: AfdianRandomCreator,
    val avatarBitmap: Bitmap? = null
)

class AfdianRandomCreatorWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isDark = resolveIsDark(context)
        val (data, errorMsg) = loadCreator(context)
        provideContent {
            AfdianRandomCreatorContent(data, errorMsg, isDark)
        }
    }

    private suspend fun loadCreator(context: Context): Pair<RandomCreatorDisplayData?, String?> {
        val prefs = AfdianPreferences(context)
        return try {
            val creator = fetchRandomCreator(prefs.cookie)
            if (creator != null) {
                Log.d(TAG, "Creator loaded: ${creator.name}, category=${creator.categoryName}")
                val avatarBitmap = downloadAvatar(context, creator.avatar)
                RandomCreatorDisplayData(creator, avatarBitmap) to null
            } else {
                null to "获取失败"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed: ${e.message}")
            null to e.message
        }
    }

    private suspend fun fetchRandomCreator(cookie: String): AfdianRandomCreator? {
        return withContext(Dispatchers.IO) {
            val maxRetries = 3
            for (attempt in 1..maxRetries) {
                try {
                    val randomPage = (1..20).random()
                    val urlStr = "https://ifdian.net/api/creator/list?page=$randomPage&type=hot&category_id=&q="
                    Log.d(TAG, "Fetching (attempt $attempt): $urlStr")
                    
                    val normalizedCookie = if (cookie.startsWith("auth_token=")) cookie else "auth_token=$cookie"
                    val url = java.net.URL(urlStr)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("Cookie", normalizedCookie)
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows) AppleWebKit/537.36 Chrome/122.0.0.0")
                    conn.setRequestProperty("referer", "https://afdian.com/")
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000
                    conn.instanceFollowRedirects = true
                    
                    val bodyText = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    
                    Log.d(TAG, "Creator resp (attempt $attempt): ${bodyText.take(300)}")
                    
                    val json = Json { ignoreUnknownKeys = true }
                    val result = json.decodeFromString<AfdianCreatorListResponse>(bodyText)
                    if (result.ec != 200) continue

                    val creators = result.data?.list
                    if (creators.isNullOrEmpty()) continue

                    val item = creators.random()
                    return@withContext AfdianRandomCreator(
                        userId = item.user_id,
                        name = item.name,
                        avatar = item.avatar,
                        urlSlug = item.url_slug,
                        isVerified = item.is_verified == 1,
                        doing = item.creator?.doing ?: "",
                        categoryName = item.creator?.category?.name ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "fetchRandomCreator (attempt $attempt) err: ${e.message}")
                }
            }
            null
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
                if (bytes == null || bytes.isEmpty()) {
                    Log.e(TAG, "Avatar download: empty response")
                    return@withContext null
                }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap == null) {
                    Log.e(TAG, "Avatar decode: BitmapFactory returned null (${bytes.size} bytes)")
                }
                bitmap
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download avatar: ${e.javaClass.simpleName}: ${e.message}")
                null
            }
        }
    }

    companion object {
        private const val TAG = "AfdianRandomCreator"

        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(AfdianRandomCreatorWidget::class.java).forEach { id ->
                AfdianRandomCreatorWidget().update(context, id)
            }
        }
    }
}

class RefreshAfdianRandomCreatorAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AfdianRandomCreatorWidget().update(context, glanceId)
    }
}

class OpenAfdianCreatorAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val url = parameters[creatorUrlParam] ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

private val creatorUrlParam = ActionParameters.Key<String>("creator_url")

@Composable
private fun AfdianRandomCreatorContent(data: RandomCreatorDisplayData?, errorMsg: String?, isDark: Boolean) {
    val size = LocalSize.current
    val isCompact = size.width < 200.dp

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

    val creatorUrl = data?.creator?.urlSlug?.takeIf { it.isNotBlank() }?.let { "https://ifdian.net/@$it" }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable(
                onClick = actionRunCallback<RefreshAfdianRandomCreatorAction>()
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
            val creator = data!!.creator
            Column(modifier = GlanceModifier.fillMaxSize()) {
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
                        text = "发现创作者",
                        style = TextStyle(
                            color = ColorProvider(labelColor),
                            fontSize = 13.sp
                        )
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    if (creatorUrl != null) {
                        Text(
                            text = "@${creator.urlSlug}",
                            style = TextStyle(
                                color = ColorProvider(accentColor),
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(GlanceModifier.height(8.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (data.avatarBitmap != null) {
                        Image(
                            provider = BitmapImageProvider(data.avatarBitmap),
                            contentDescription = null,
                            modifier = GlanceModifier
                                .size(if (isCompact) 48.dp else 56.dp)
                                .cornerRadius(12.dp)
                        )
                        Spacer(GlanceModifier.width(10.dp))
                    }
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = creator.name,
                                style = TextStyle(
                                    color = ColorProvider(headerTextColor),
                                    fontSize = if (isCompact) 15.sp else 17.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                            if (creator.isVerified) {
                                Spacer(GlanceModifier.width(4.dp))
                                Image(
                                    provider = ImageProvider(R.drawable.ic_verified_badge),
                                    contentDescription = "已认证",
                                    modifier = GlanceModifier.size(18.dp)
                                )
                            }
                        }
                        if (creator.categoryName.isNotBlank()) {
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                text = creator.categoryName,
                                style = TextStyle(
                                    color = ColorProvider(accentColor),
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                if (creator.doing.isNotBlank()) {
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        text = creator.doing,
                        style = TextStyle(
                            color = ColorProvider(labelColor),
                            fontSize = 12.sp
                        ),
                        maxLines = if (isCompact) 2 else 3
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                Text(
                    text = "点击刷新",
                    style = TextStyle(
                        color = ColorProvider(labelColor.copy(alpha = 0.5f)),
                        fontSize = 10.sp
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

class AfdianRandomCreatorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AfdianRandomCreatorWidget()
}
