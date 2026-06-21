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
import cn.waijade.nexuswid.data.afdian.AfdianPlan
import cn.waijade.nexuswid.data.afdian.AfdianPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class ProductDisplayData(
    val plan: AfdianPlan,
    val imageBitmap: Bitmap? = null
)

class AfdianProductWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = AfdianPreferences(context)
        val isDark = resolveIsDark(context)
        val (data, errorMsg) = loadProduct(context)
        provideContent {
            AfdianProductContent(data, errorMsg, isDark, prefs.isConfigured)
        }
    }

    private suspend fun loadProduct(context: Context): Pair<ProductDisplayData?, String?> {
        val prefs = AfdianPreferences(context)
        if (!prefs.isConfigured) {
            Log.d(TAG, "Not configured")
            return null to null
        }

        val planId = prefs.selectedProductPlanId
        if (planId.isBlank()) {
            Log.d(TAG, "No product selected")
            return null to "选择商品"
        }

        val json = Json { ignoreUnknownKeys = true }
        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
        }
        val service = AfdianApiService(httpClient, json)
        return try {
            val plans = service.getPlans(prefs.cookie)
            val found = plans.find { it.plan_id == planId }
            if (found != null) {
                Log.d(TAG, "Product loaded: ${found.name}, amount=${found.total_amount}, sales=${found.sponsor_count}")
                val imageBitmap = downloadProductImage(context, found.pic)
                ProductDisplayData(found, imageBitmap) to null
            } else {
                null to "未找到商品"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed: ${e.message}")
            null to e.message
        } finally {
            httpClient.close()
        }
    }

    private suspend fun downloadProductImage(context: Context, picUrl: String): Bitmap? {
        if (picUrl.isBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = okhttp3.Request.Builder().url(picUrl).build()
                val response = client.newCall(request).execute()
                val bytes = response.body?.bytes()
                response.close()
                client.dispatcher.executorService.shutdown()
                if (bytes == null || bytes.isEmpty()) {
                    Log.e(TAG, "Image download: empty response")
                    return@withContext null
                }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap == null) {
                    Log.e(TAG, "Image decode: BitmapFactory returned null (${bytes.size} bytes)")
                }
                bitmap
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download image: ${e.javaClass.simpleName}: ${e.message}")
                null
            }
        }
    }

    companion object {
        private const val TAG = "AfdianProductWidget"

        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(AfdianProductWidget::class.java).forEach { id ->
                AfdianProductWidget().update(context, id)
            }
        }
    }
}

class OpenAfdianProductConfigAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, cn.waijade.nexuswid.AfdianProductConfigActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class RefreshAfdianProductAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AfdianProductWidget().update(context, glanceId)
    }
}

@Composable
private fun AfdianProductContent(data: ProductDisplayData?, errorMsg: String?, isDark: Boolean, isConfigured: Boolean) {
    val product = data?.plan
    val totalAmount = product?.total_amount?.toDoubleOrNull() ?: 0.0
    val totalSales = product?.sponsor_count ?: 0
    val profit = totalAmount * 0.94

    val statusText = when {
        !isConfigured -> "未配置"
        product == null && !errorMsg.isNullOrBlank() -> errorMsg
        product == null -> "加载中"
        else -> ""
    }

    val bgColor = if (isDark) ComposeColor(0xFF0D1117) else ComposeColor(0xFFF6F8FA)
    val headerTextColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val labelColor = if (isDark) ComposeColor.White.copy(alpha = 0.7f) else ComposeColor(0xFF656D76)
    val accentColor = ComposeColor(0xFF946CE6)
    val dividerColor = if (isDark) ComposeColor(0xFF30363D) else ComposeColor(0xFFD0D7DE)
    val iconRes = if (isDark) R.drawable.ic_afdian_dark else R.drawable.ic_afdian

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clickable(onClick = actionRunCallback<OpenAfdianProductConfigAction>())
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().height(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = null,
                    modifier = GlanceModifier.size(18.dp)
                )
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "商品收益",
                    style = TextStyle(
                        color = ColorProvider(headerTextColor),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
            }

            Spacer(GlanceModifier.height(6.dp))

            if (statusText.isNotBlank()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        style = TextStyle(
                            color = ColorProvider(labelColor),
                            fontSize = 12.sp
                        )
                    )
                }
            } else {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (data?.imageBitmap != null) {
                        Image(
                            provider = BitmapImageProvider(data.imageBitmap),
                            contentDescription = null,
                            modifier = GlanceModifier.size(56.dp)
                        )
                        Spacer(GlanceModifier.width(12.dp))
                    }
                    Column {
                        Text(
                            text = product!!.name,
                            style = TextStyle(
                                color = ColorProvider(headerTextColor),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = "¥${product.price}",
                            style = TextStyle(
                                color = ColorProvider(accentColor),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(GlanceModifier.height(6.dp))

                Box(
                    modifier = GlanceModifier
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(dividerColor)
                ) {}

                Spacer(GlanceModifier.height(6.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem("收入", formatAmount(totalAmount), accentColor, GlanceModifier.defaultWeight())
                    StatItem("销量", formatCount(totalSales), accentColor, GlanceModifier.defaultWeight())
                    StatItem("利润", formatAmount(profit), accentColor, GlanceModifier.defaultWeight())
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: ComposeColor, modifier: GlanceModifier = GlanceModifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(color),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(ComposeColor(0xFF8B949E)),
                fontSize = 11.sp
            )
        )
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

private fun formatCount(count: Int): String {
    return count.toString()
}

class AfdianProductWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AfdianProductWidget()
}
