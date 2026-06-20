package cn.waijade.nexuswid.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
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
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.data.github.IssueItem
import cn.waijade.nexuswid.data.github.IssueType
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val MAX_VISIBLE_ROWS = 8
private const val ISSUE_LIST_LIMIT = 20

class IssuesWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadData(context)
        val prefs = GitHubPreferences(context)
        val isDark = resolveIsDark(context, prefs.widgetColorMode)
        provideContent {
            IssuesContent(data, isDark)
        }
    }

    private suspend fun loadData(context: Context): IssuesData {
        val prefs = GitHubPreferences(context)
        val types = prefs.selectedIssueTypes

        if (prefs.debugUseTestData) {
            return IssuesData(
                count = 5,
                items = listOf(
                    IssueItem("github/docs", 4287, "Fix broken links in API reference", "", "open"),
                    IssueItem("kubernetes/kubernetes", 131204, "Update node autoscaler config", "", "open"),
                    IssueItem("jetbrains/compose-multiplatform", 5392, "Add support for Material3 dynamic colors", "", "open"),
                    IssueItem("google/accompanist", 1891, "Update SwipeRefresh to use Material3 pull-to-refresh", "", "open"),
                    IssueItem("square/okhttp", 8234, "Fix connection pool leak on timeout", "", "open")
                ),
                types = types
            )
        }

        val token = prefs.token.takeIf { it.isNotBlank() }
            ?: return IssuesData(count = -1, items = emptyList(), types = types)

        val json = Json { ignoreUnknownKeys = true }
        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
        }
        val service = GitHubApiService(httpClient, json)

        val list = runCatching {
            service.getIssueList(
                token = token,
                issueTypes = types,
                limit = ISSUE_LIST_LIMIT
            ).getOrThrow()
        }.getOrElse { emptyList() }

        httpClient.close()

        return IssuesData(
            count = list.size,
            items = list,
            types = types
        )
    }

    companion object {
        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(IssuesWidget::class.java).forEach { id ->
                IssuesWidget().update(context, id)
            }
        }
    }
}

private data class IssuesData(
    val count: Int,
    val items: List<IssueItem>,
    val types: Set<IssueType>
)

@Composable
private fun IssuesContent(data: IssuesData, isDark: Boolean) {
    val size = LocalSize.current
    val rowHeight = 56.dp
    val headerHeight = 44.dp
    val available = (size.height - headerHeight).coerceAtLeast(0.dp)
    val rows = (available.value / rowHeight.value).toInt().coerceIn(1, MAX_VISIBLE_ROWS)

    val visibleItems = data.items.take(rows)
    val title = headerTitle(data.count, data.types)

    val bgColor = if (isDark) ComposeColor(0xFF0D1117) else ComposeColor(0xFFF6F8FA)
    val headerTextColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val repoTextColor = if (isDark) ComposeColor(0xFF8B949E) else ComposeColor(0xFF656D76)
    val rowTitleColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val emptyTextColor = if (isDark) ComposeColor(0xFF8B949E) else ComposeColor(0xFF656D76)
    val ghLogoRes = if (isDark) R.drawable.ic_mark_github else R.drawable.ic_mark_github_dark
    val issueIconRes = if (isDark) R.drawable.ic_git_issue else R.drawable.ic_git_issue_dark

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            HeaderRow(title, headerTextColor, ghLogoRes, issueIconRes)
            Spacer(GlanceModifier.height(10.dp))
            if (data.count < 0) {
                EmptyHint("Sign in GitHub to see issues", emptyTextColor)
            } else if (visibleItems.isEmpty()) {
                EmptyHint("No issues", emptyTextColor)
            } else {
                IssueList(visibleItems, repoTextColor, rowTitleColor)
            }
        }
    }
}

@Composable
private fun HeaderRow(title: String, textColor: ComposeColor, ghLogoRes: Int, issueIconRes: Int) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(issueIconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp)
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Image(
            provider = ImageProvider(ghLogoRes),
            contentDescription = null,
            modifier = GlanceModifier.size(24.dp)
        )
    }
}

@Composable
private fun EmptyHint(text: String, color: ComposeColor) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = ColorProvider(color),
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun IssueList(items: List<IssueItem>, repoTextColor: ComposeColor, rowTitleColor: ComposeColor) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        items.forEach { issue ->
            IssueRow(issue, repoTextColor, rowTitleColor)
        }
    }
}

@Composable
private fun IssueRow(issue: IssueItem, repoTextColor: ComposeColor, rowTitleColor: ComposeColor) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${issue.repoFullName} #${issue.number}",
            style = TextStyle(
                color = ColorProvider(repoTextColor),
                fontSize = 13.sp
            ),
            maxLines = 1
        )
        Spacer(GlanceModifier.height(1.dp))
        Text(
            text = issue.title,
            style = TextStyle(
                color = ColorProvider(rowTitleColor),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

private fun resolveIsDark(context: Context, mode: HeatmapColorMode): Boolean {
    return when (mode) {
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
        HeatmapColorMode.SYSTEM -> {
            val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightMask == Configuration.UI_MODE_NIGHT_YES
        }
    }
}

private fun headerTitle(count: Int, types: Set<IssueType>): String {
    if (count < 0) return "Issues"
    val n = count.coerceAtLeast(0)
    val onlyAssigned = types.size == 1 && types.contains(IssueType.ASSIGNED)
    return if (onlyAssigned) {
        "$n assigned issues"
    } else {
        "$n issues"
    }
}

class IssuesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = IssuesWidget()
}
