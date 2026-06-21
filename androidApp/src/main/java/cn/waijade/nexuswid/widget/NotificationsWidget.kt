package cn.waijade.nexuswid.widget

import android.content.Context
import android.content.res.Configuration
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
import androidx.glance.layout.wrapContentHeight
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import cn.waijade.nexuswid.R
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.data.github.NotificationItem
import cn.waijade.nexuswid.data.github.NotificationSubjectType
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val MAX_VISIBLE_ROWS = 30
private const val NOTIFICATION_LIST_LIMIT = 30

class NotificationsWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadData(context)
        val prefs = GitHubPreferences(context)
        val isDark = resolveIsDark(context, prefs.widgetColorMode)
        provideContent {
            NotificationsContent(data, isDark)
        }
    }

    private suspend fun loadData(context: Context): NotificationsData {
        val prefs = GitHubPreferences(context)

        if (prefs.debugUseTestData) {
            return NotificationsData(
                count = 5,
                unreadCount = 3,
                items = listOf(
                    NotificationItem(
                        id = "1",
                        repoFullName = "AstralSightStudios/AstroBox-Repo",
                        title = "Add new quick app entry for 恐龙快跑",
                        subjectType = NotificationSubjectType.PULL_REQUEST,
                        reason = cn.waijade.nexuswid.data.github.NotificationReason.COMMENT,
                        isUnread = true,
                        updatedAt = "2026-06-19T03:49:27Z",
                        htmlUrl = "",
                        state = "closed"
                    ),
                    NotificationItem(
                        id = "2",
                        repoFullName = "google/accompanist",
                        title = "Update SwipeRefresh to use Material3 pull-to-refresh",
                        subjectType = NotificationSubjectType.PULL_REQUEST,
                        reason = cn.waijade.nexuswid.data.github.NotificationReason.REVIEW_REQUESTED,
                        isUnread = true,
                        updatedAt = "2026-06-18T10:30:00Z",
                        htmlUrl = "",
                        state = "merged"
                    ),
                    NotificationItem(
                        id = "3",
                        repoFullName = "kubernetes/kubernetes",
                        title = "Fix node autoscaler memory leak",
                        subjectType = NotificationSubjectType.ISSUE,
                        reason = cn.waijade.nexuswid.data.github.NotificationReason.MENTION,
                        isUnread = true,
                        updatedAt = "2026-06-17T15:20:00Z",
                        htmlUrl = "",
                        state = "closed"
                    ),
                    NotificationItem(
                        id = "4",
                        repoFullName = "square/okhttp",
                        title = "Release v5.0.0-alpha.14",
                        subjectType = NotificationSubjectType.RELEASE,
                        reason = cn.waijade.nexuswid.data.github.NotificationReason.SUBSCRIBED,
                        isUnread = false,
                        updatedAt = "2026-06-16T09:00:00Z",
                        htmlUrl = "",
                        state = null
                    ),
                    NotificationItem(
                        id = "5",
                        repoFullName = "jetbrains/compose-multiplatform",
                        title = "Discussion: iOS target stability",
                        subjectType = NotificationSubjectType.DISCUSSION,
                        reason = cn.waijade.nexuswid.data.github.NotificationReason.COMMENT,
                        isUnread = false,
                        updatedAt = "2026-06-15T14:45:00Z",
                        htmlUrl = "",
                        state = null
                    )
                )
            )
        }

        val token = prefs.token.takeIf { it.isNotBlank() }
            ?: return NotificationsData(count = -1, unreadCount = 0, items = emptyList())

        val json = Json { ignoreUnknownKeys = true }
        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(json) }
        }
        val service = GitHubApiService(httpClient, json)

        val result = runCatching {
            service.getNotifications(
                token = token,
                limit = NOTIFICATION_LIST_LIMIT
            ).getOrThrow()
        }

        httpClient.close()

        val list = result.getOrElse {
            val cached = WidgetDataCache.loadNotifications(context)
            val unreadCount = cached.count { it.isUnread }
            return NotificationsData(
                count = cached.size,
                unreadCount = unreadCount,
                items = cached
            )
        }

        WidgetDataCache.saveNotifications(context, list)

        val unreadCount = list.count { it.isUnread }

        return NotificationsData(
            count = list.size,
            unreadCount = unreadCount,
            items = list
        )
    }

    companion object {
        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(NotificationsWidget::class.java).forEach { id ->
                NotificationsWidget().update(context, id)
            }
        }
    }
}

private data class NotificationsData(
    val count: Int,
    val unreadCount: Int,
    val items: List<NotificationItem>
)

@Composable
private fun NotificationsContent(data: NotificationsData, isDark: Boolean) {
    val size = LocalSize.current
    val headerHeight = 44.dp
    val minRowHeight = 60.dp
    val available = (size.height - headerHeight).coerceAtLeast(0.dp)
    val rows = (available.value / minRowHeight.value).toInt().coerceIn(1, MAX_VISIBLE_ROWS)
    val rowHeight = if (rows > 0) (available.value / rows).dp else minRowHeight

    val visibleItems = data.items.take(rows)
    val title = if (data.count < 0) "Notifications" else "${data.unreadCount} unread"

    val bgColor = if (isDark) ComposeColor(0xFF0D1117) else ComposeColor(0xFFF6F8FA)
    val headerTextColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val repoTextColor = if (isDark) ComposeColor(0xFF8B949E) else ComposeColor(0xFF656D76)
    val rowTitleColor = if (isDark) ComposeColor.White else ComposeColor(0xFF1F2328)
    val emptyTextColor = if (isDark) ComposeColor(0xFF8B949E) else ComposeColor(0xFF656D76)
    val ghLogoRes = if (isDark) R.drawable.ic_mark_github else R.drawable.ic_mark_github_dark
    val notificationsIconRes = if (isDark) R.drawable.ic_notifications else R.drawable.ic_notifications_dark

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clickable(onClick = actionRunCallback<RefreshNotificationsAction>())
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            HeaderRow(title, headerTextColor, ghLogoRes, notificationsIconRes)
            Spacer(GlanceModifier.height(10.dp))
            if (data.count < 0) {
                EmptyHint("Sign in GitHub to see notifications", emptyTextColor)
            } else if (visibleItems.isEmpty()) {
                EmptyHint("No notifications", emptyTextColor)
            } else {
                NotificationList(visibleItems, repoTextColor, rowTitleColor, rowHeight)
            }
        }
    }
}

@Composable
private fun HeaderRow(title: String, textColor: ComposeColor, ghLogoRes: Int, notificationsIconRes: Int) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(notificationsIconRes),
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
private fun NotificationList(items: List<NotificationItem>, repoTextColor: ComposeColor, rowTitleColor: ComposeColor, rowHeight: Dp) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        items.forEach { notification ->
            NotificationRow(notification, repoTextColor, rowTitleColor, rowHeight)
        }
    }
}

@Composable
private fun NotificationRow(notification: NotificationItem, repoTextColor: ComposeColor, rowTitleColor: ComposeColor, rowHeight: Dp) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubjectTypeIcon(notification.subjectType, notification.state)
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = notification.repoFullName,
                style = TextStyle(
                    color = ColorProvider(repoTextColor),
                    fontSize = 12.sp
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
            ReasonBadge(notification.reason, repoTextColor)
            if (notification.isUnread) {
                Spacer(GlanceModifier.width(4.dp))
                UnreadDot()
            }
        }
        Spacer(GlanceModifier.height(1.dp))
        Text(
            text = notification.title,
            style = TextStyle(
                color = ColorProvider(rowTitleColor),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun SubjectTypeIcon(type: NotificationSubjectType, state: String? = null) {
    val res = when (type) {
        NotificationSubjectType.ISSUE -> when (state) {
            "closed" -> R.drawable.ic_issue_closed
            else -> R.drawable.ic_issue_opened
        }
        NotificationSubjectType.PULL_REQUEST -> when (state) {
            "closed" -> R.drawable.ic_git_pull_request_closed
            "merged" -> R.drawable.ic_git_merge
            else -> R.drawable.ic_git_pull_request_open
        }
        NotificationSubjectType.COMMIT -> R.drawable.ic_commit
        NotificationSubjectType.RELEASE -> R.drawable.ic_tag
        NotificationSubjectType.DISCUSSION -> R.drawable.ic_comment
        NotificationSubjectType.CHECK_SUITE -> R.drawable.ic_check
        else -> R.drawable.ic_notifications
    }
    Image(
        provider = ImageProvider(res),
        contentDescription = null,
        modifier = GlanceModifier.size(14.dp)
    )
}

@Composable
private fun ReasonBadge(reason: cn.waijade.nexuswid.data.github.NotificationReason, color: ComposeColor) {
    Text(
        text = reason.displayName,
        style = TextStyle(
            color = ColorProvider(color),
            fontSize = 10.sp
        ),
        maxLines = 1
    )
}

@Composable
private fun UnreadDot() {
    Box(
        modifier = GlanceModifier
            .size(6.dp)
            .background(ComposeColor(0xFF1F883D))
            .cornerRadius(3.dp)
    ) {}
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

class RefreshNotificationsAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        NotificationsWidget().update(context, glanceId)
    }
}

class NotificationsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NotificationsWidget()
}
