package cn.waijade.nexuswid.widget

import android.content.Context
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.glance.unit.ColorProvider
import cn.waijade.nexuswid.R
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.data.github.PullRequestType
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ReviewsRequestedWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val count = loadCount(context)
        provideContent {
            ReviewsRequestedContent(count)
        }
    }

    private suspend fun loadCount(context: Context): Int {
        val prefs = GitHubPreferences(context)
        val debugValue = prefs.debugCountValue
        if (debugValue >= 0) return debugValue

        val token = prefs.token.takeIf { it.isNotBlank() } ?: return -1
        val pullRequestTypes = setOf(PullRequestType.REVIEW_REQUESTED)

        return runCatching {
            val json = Json { ignoreUnknownKeys = true }
            val httpClient = HttpClient(OkHttp) {
                install(ContentNegotiation) { json(json) }
            }
            val apiService = GitHubApiService(httpClient, json)
            apiService.getPullRequestCount(token, pullRequestTypes).getOrThrow()
        }.getOrElse { -1 }
    }
}

class RefreshReviewsRequestedAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        ReviewsRequestedWidget().update(context, glanceId)
    }
}

@Composable
private fun ReviewsRequestedContent(count: Int) {
    val size = LocalSize.current
    val edge: Dp = size.width.coerceAtMost(size.height)
    val pad = 14.dp
    val contentEdge = edge - pad * 2

    val countText = if (count >= 0) formatCount(count) else "_"

    val iconSize: Dp = (contentEdge * 0.15f).coerceIn(12.dp, 24.dp)
    val labelSize: Dp = (contentEdge * 0.12f).coerceIn(8.dp, 14.dp)

    val maxByHeight: Dp = contentEdge * 0.45f
    val maxByWidth: Dp = contentEdge / (countText.length * 0.55f + 0.5f)
    val clampedFontSize: Dp = maxByHeight.coerceAtMost(maxByWidth).coerceIn(10.dp, 64.dp)

    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = GlanceModifier
                .size(edge)
                .cornerRadius(28.dp)
                .background(ComposeColor(0xFF000000))
                .padding(pad)
                .clickable(onClick = actionRunCallback<RefreshReviewsRequestedAction>())
        ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Image(
                provider = ImageProvider(R.drawable.ic_git_pull_request),
                contentDescription = null,
                modifier = GlanceModifier.size(iconSize)
            )
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = countText,
                    style = TextStyle(
                        color = ColorProvider(ComposeColor.White),
                        fontSize = clampedFontSize.value.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
            Column {
                Text(
                    text = "Reviews",
                    style = TextStyle(
                        color = ColorProvider(ComposeColor.White.copy(alpha = 0.7f)),
                        fontSize = labelSize.value.sp
                    )
                )
                Text(
                    text = "Requested",
                    style = TextStyle(
                        color = ColorProvider(ComposeColor.White.copy(alpha = 0.7f)),
                        fontSize = labelSize.value.sp
                    )
                )
            }
        }
    }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M+"
        count >= 1_000 -> "${count / 1_000}k+"
        else -> count.toString()
    }
}

class ReviewsRequestedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ReviewsRequestedWidget()
}
