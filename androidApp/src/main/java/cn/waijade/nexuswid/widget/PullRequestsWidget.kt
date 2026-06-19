package cn.waijade.nexuswid.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
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
import androidx.glance.layout.wrapContentHeight
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.glance.unit.ColorProvider
import cn.waijade.nexuswid.R
import cn.waijade.nexuswid.data.github.CheckStatus
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.data.github.PullRequestItem
import cn.waijade.nexuswid.data.github.PullRequestType
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val MAX_VISIBLE_ROWS = 8
private const val PR_LIST_LIMIT = 20

class PullRequestsWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadData(context)
        provideContent {
            GlanceTheme {
                PullRequestsContent(data)
            }
        }
    }

    private suspend fun loadData(context: Context): PullRequestsData {
        val prefs = GitHubPreferences(context)
        val types = prefs.selectedPullRequestTypes

        if (prefs.debugUseTestData) {
            return PullRequestsData(
                count = 5,
                items = listOf(
                    PullRequestItem("github/docs", 4287, "Fix broken links in API reference", "", "", CheckStatus.SUCCESS),
                    PullRequestItem("kubernetes/kubernetes", 131204, "Update node autoscaler config", "", "", CheckStatus.FAILURE),
                    PullRequestItem("jetbrains/compose-multiplatform", 5392, "Add support for Material3 dynamic colors", "", "", CheckStatus.PENDING),
                    PullRequestItem("google/accompanist", 1891, "Update SwipeRefresh to use Material3 pull-to-refresh", "", "", CheckStatus.NONE),
                    PullRequestItem("square/okhttp", 8234, "Fix connection pool leak on timeout", "", "", CheckStatus.SUCCESS)
                ),
                types = types
            )
        }

        val token = prefs.token.takeIf { it.isNotBlank() }
            ?: return PullRequestsData(count = -1, items = emptyList(), types = types)

        val json = Json { ignoreUnknownKeys = true }
        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
        }
        val service = GitHubApiService(httpClient, json)

        val list = runCatching {
            service.getPullRequestList(
                token = token,
                pullRequestTypes = types,
                limit = PR_LIST_LIMIT,
                withCheckStatus = MAX_VISIBLE_ROWS
            ).getOrThrow()
        }.getOrElse { emptyList() }

        httpClient.close()

        return PullRequestsData(
            count = list.size,
            items = list,
            types = types
        )
    }
}

private data class PullRequestsData(
    val count: Int,
    val items: List<PullRequestItem>,
    val types: Set<PullRequestType>
)

@Composable
private fun PullRequestsContent(data: PullRequestsData) {
    val size = LocalSize.current
    val rowHeight = 56.dp
    val headerHeight = 44.dp
    val available = (size.height - headerHeight).coerceAtLeast(0.dp)
    val rows = (available.value / rowHeight.value).toInt().coerceIn(1, MAX_VISIBLE_ROWS)

    val visibleItems = data.items.take(rows)
    val title = headerTitle(data.count, data.types)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(ComposeColor(0xFF0D1117))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            HeaderRow(title)
            Spacer(GlanceModifier.height(10.dp))
            if (data.count < 0) {
                EmptyHint("Sign in GitHub to see pull requests")
            } else if (visibleItems.isEmpty()) {
                EmptyHint("No pull requests")
            } else {
                PullRequestList(visibleItems)
            }
        }
    }
}

@Composable
private fun HeaderRow(title: String) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_git_pull_request_green),
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp)
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(ComposeColor.White),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Image(
            provider = ImageProvider(R.drawable.ic_mark_github),
            contentDescription = null,
            modifier = GlanceModifier.size(24.dp)
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = ColorProvider(ComposeColor(0xFF8B949E)),
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun PullRequestList(items: List<PullRequestItem>) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        items.forEach { pr ->
            PullRequestRow(pr)
        }
    }
}

@Composable
private fun PullRequestRow(pr: PullRequestItem) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${pr.repoFullName} #${pr.number}",
                style = TextStyle(
                    color = ColorProvider(ComposeColor(0xFF8B949E)),
                    fontSize = 13.sp
                ),
                maxLines = 1
            )
            Spacer(GlanceModifier.width(4.dp))
            CheckStatusIcon(pr.checkStatus)
        }
        Spacer(GlanceModifier.height(1.dp))
        Text(
            text = pr.title,
            style = TextStyle(
                color = ColorProvider(ComposeColor.White),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun CheckStatusIcon(status: CheckStatus) {
    val res = when (status) {
        CheckStatus.SUCCESS -> R.drawable.ic_check_circle_green
        CheckStatus.FAILURE -> R.drawable.ic_x_circle_red
        CheckStatus.PENDING -> R.drawable.ic_dot_circle_gray
        CheckStatus.NONE -> null
    }
    if (res != null) {
        Image(
            provider = ImageProvider(res),
            contentDescription = null,
            modifier = GlanceModifier.size(16.dp)
        )
    } else {
        Spacer(GlanceModifier.size(16.dp))
    }
}

private fun headerTitle(count: Int, types: Set<PullRequestType>): String {
    if (count < 0) return "Pull requests"
    val n = count.coerceAtLeast(0)
    val onlyReview = types.size == 1 && types.contains(PullRequestType.REVIEW_REQUESTED)
    return if (onlyReview) {
        "$n reviews requested"
    } else {
        "$n pull requests"
    }
}

class PullRequestsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PullRequestsWidget()
}
