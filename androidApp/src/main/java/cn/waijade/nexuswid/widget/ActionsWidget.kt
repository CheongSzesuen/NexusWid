package cn.waijade.nexuswid.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.wrapContentHeight
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.glance.unit.ColorProvider
import cn.waijade.nexuswid.ActionsConfigActivity
import cn.waijade.nexuswid.R
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.data.github.WorkflowRunItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val EXTRA_CONFIGURE_ACTIONS_WIDGET = "configure_actions_widget"
private const val PREFS_NAME = "github_preferences"
private const val KEY_ACTIONS_REPO_PREFIX = "actions_repo_"

private const val MAX_VISIBLE_ROWS = 8

fun getActionsRepoForWidget(context: Context, widgetId: String): String {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString("$KEY_ACTIONS_REPO_PREFIX$widgetId", "") ?: ""
}

fun setActionsRepoForWidget(context: Context, widgetId: String, repo: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString("$KEY_ACTIONS_REPO_PREFIX$widgetId", repo)
        .commit()
}

class ActionsWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetId = id.toString()
        val repo = getActionsRepoForWidget(context, widgetId)
        val data = loadData(context, widgetId)
        val prefs = GitHubPreferences(context)
        val isDark = resolveIsDark(context, prefs.widgetColorMode)
        provideContent {
            ActionsContent(data, isDark)
        }
    }

    private suspend fun loadData(context: Context, widgetId: String): ActionsData {
        val repo = getActionsRepoForWidget(context, widgetId)
        if (repo.isBlank()) {
            return ActionsData(repo = "", widgetId = widgetId, runs = emptyList(), error = null)
        }

        val prefs = GitHubPreferences(context)
        val token = prefs.token.takeIf { it.isNotBlank() }
            ?: return ActionsData(repo = repo, widgetId = widgetId, runs = emptyList(), error = "Sign in first")

        val json = Json { ignoreUnknownKeys = true }
        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
        }
        val service = GitHubApiService(httpClient, json)

        val result = runCatching {
            service.getWorkflowRuns(token = token, repo = repo, limit = 50).getOrThrow()
        }

        httpClient.close()

        return result.fold(
            onSuccess = { runs ->
                ActionsData(repo = repo, widgetId = widgetId, runs = runs, error = null)
            },
            onFailure = { e ->
                ActionsData(repo = repo, widgetId = widgetId, runs = emptyList(), error = e.message ?: "Failed to load")
            }
        )
    }

    companion object {
        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(ActionsWidget::class.java).forEach { id ->
                ActionsWidget().update(context, id)
            }
        }
    }
}

private data class ActionsData(
    val repo: String,
    val widgetId: String,
    val runs: List<WorkflowRunItem>,
    val error: String?
)

@Composable
private fun ActionsContent(data: ActionsData, isDark: Boolean) {
    val size = LocalSize.current
    val rowHeight = 48.dp
    val headerHeight = 44.dp
    val available = (size.height - headerHeight).coerceAtLeast(0.dp)
    val rows = (available.value / rowHeight.value).toInt().coerceIn(1, MAX_VISIBLE_ROWS)

    val visibleRuns = data.runs.take(rows)

    val bgColor = if (isDark) ComposeColor(0xFF0D1117) else ComposeColor(0xFFF6F8FA)
    val headerTextColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val branchTextColor = if (isDark) ComposeColor(0xFF8B949E) else ComposeColor(0xFF656D76)
    val rowTitleColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val emptyTextColor = if (isDark) ComposeColor(0xFF8B949E) else ComposeColor(0xFF656D76)
    val ghLogoRes = if (isDark) R.drawable.ic_mark_github else R.drawable.ic_mark_github_dark
    val playIconRes = if (isDark) R.drawable.ic_play else R.drawable.ic_play_dark

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clickable(onClick = actionRunCallback<OpenActionsConfigAction>())
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            HeaderRow(data.repo, headerTextColor, ghLogoRes, playIconRes)
            Spacer(GlanceModifier.height(10.dp))
            when {
                data.repo.isBlank() -> EmptyHint("Tap to set repo", emptyTextColor)
                data.error != null && data.runs.isEmpty() -> EmptyHint(data.error, emptyTextColor)
                visibleRuns.isEmpty() -> EmptyHint("No workflow runs", emptyTextColor)
                else -> WorkflowRunList(visibleRuns, branchTextColor, rowTitleColor)
            }
        }
    }
}

@Composable
private fun HeaderRow(repo: String, textColor: ComposeColor, ghLogoRes: Int, playIconRes: Int) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(playIconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp)
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = repo.ifBlank { "Actions" },
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
private fun WorkflowRunList(items: List<WorkflowRunItem>, branchTextColor: ComposeColor, rowTitleColor: ComposeColor) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        items.forEach { run ->
            WorkflowRunRow(run, branchTextColor, rowTitleColor)
        }
    }
}

@Composable
private fun WorkflowRunRow(run: WorkflowRunItem, branchTextColor: ComposeColor, rowTitleColor: ComposeColor) {
    val triggerTime = formatTime(run.runStartedAt ?: run.updatedAt)
    val runDuration = formatDuration(run)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WorkflowStatusIcon(run.status, run.conclusion)
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = run.workflowName,
                    style = TextStyle(
                        color = ColorProvider(rowTitleColor),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
            Spacer(GlanceModifier.height(1.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(GlanceModifier.width(22.dp))
                Text(
                    text = "#${run.runNumber}: ${eventLabel(run)}",
                    style = TextStyle(
                        color = ColorProvider(branchTextColor),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_calendar),
                    contentDescription = null,
                    modifier = GlanceModifier.size(12.dp)
                )
                Spacer(GlanceModifier.width(2.dp))
                Text(
                    text = triggerTime,
                    style = TextStyle(
                        color = ColorProvider(branchTextColor),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
            Spacer(GlanceModifier.height(1.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_stopwatch),
                    contentDescription = null,
                    modifier = GlanceModifier.size(12.dp)
                )
                Spacer(GlanceModifier.width(2.dp))
                Text(
                    text = runDuration,
                    style = TextStyle(
                        color = ColorProvider(branchTextColor),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

private fun eventLabel(run: WorkflowRunItem): String {
    return when (run.event) {
        "schedule" -> "Scheduled"
        "workflow_dispatch" -> "Manually run by ${run.actor ?: "unknown"}"
        "push" -> "Push"
        "pull_request" -> "PR"
        "release" -> "Release"
        else -> run.event
    }
}

@Composable
private fun WorkflowStatusIcon(status: String, conclusion: String?) {
    val res = when {
        conclusion == "success" -> R.drawable.ic_check_circle_green
        conclusion == "failure" || conclusion == "timed_out" -> R.drawable.ic_x_circle_red
        conclusion == "cancelled" || conclusion == "skipped" -> R.drawable.ic_dot_circle_gray
        status == "in_progress" || status == "queued" || status == "pending" -> R.drawable.ic_dot_circle_gray
        else -> null
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

private fun formatTime(isoString: String?): String {
    if (isoString == null) return ""
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoString.take(19)) ?: return isoString.take(10)
        val local = java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault())
        local.timeZone = java.util.TimeZone.getDefault()
        local.format(date)
    } catch (_: Exception) {
        isoString.take(10)
    }
}

private fun formatDuration(run: WorkflowRunItem): String {
    val startedAt = run.runStartedAt
    val updatedAt = run.updatedAt
    if (startedAt == null || updatedAt == null || run.status != "completed") return ""
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val start = sdf.parse(startedAt.take(19)) ?: return ""
        val end = sdf.parse(updatedAt.take(19)) ?: return ""
        val diffMs = end.time - start.time
        if (diffMs < 0) return ""
        val minutes = diffMs / 1000 / 60
        val seconds = (diffMs / 1000) % 60
        when {
            minutes > 60 -> "${minutes / 60}h${minutes % 60}m"
            minutes > 0 -> "${minutes}m${seconds}s"
            else -> "${seconds}s"
        }
    } catch (_: Exception) {
        ""
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

class OpenActionsConfigAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, ActionsConfigActivity::class.java)
        intent.putExtra(EXTRA_CONFIGURE_ACTIONS_WIDGET, glanceId.toString())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class ActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ActionsWidget()
}
