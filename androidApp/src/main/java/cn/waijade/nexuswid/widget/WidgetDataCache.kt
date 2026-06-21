package cn.waijade.nexuswid.widget

import android.content.Context
import android.content.SharedPreferences
import cn.waijade.nexuswid.data.github.CheckStatus
import cn.waijade.nexuswid.data.github.IssueItem
import cn.waijade.nexuswid.data.github.NotificationItem
import cn.waijade.nexuswid.data.github.NotificationReason
import cn.waijade.nexuswid.data.github.NotificationSubjectType
import cn.waijade.nexuswid.data.github.PullRequestItem
import cn.waijade.nexuswid.data.github.WorkflowRunItem
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object WidgetDataCache {

    private const val PREFS_NAME = "widget_data_cache"

    private const val KEY_PULL_REQUESTS = "cached_pull_requests"
    private const val KEY_ISSUES = "cached_issues"
    private const val KEY_NOTIFICATIONS = "cached_notifications"
    private const val KEY_ACTIONS_PREFIX = "cached_actions_"
    private const val KEY_REVIEWS_COUNT = "cached_reviews_count"

    private val json = Json { ignoreUnknownKeys = true }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun savePullRequests(context: Context, items: List<PullRequestItem>) {
        runCatching {
            val encoded = json.encodeToString(ListSerializer(PullRequestItem.serializer()), items)
            prefs(context).edit().putString(KEY_PULL_REQUESTS, encoded).apply()
        }
    }

    fun loadPullRequests(context: Context): List<PullRequestItem> {
        return runCatching {
            val raw = prefs(context).getString(KEY_PULL_REQUESTS, null) ?: return emptyList()
            json.decodeFromString(ListSerializer(PullRequestItem.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    fun saveIssues(context: Context, items: List<IssueItem>) {
        runCatching {
            val encoded = json.encodeToString(ListSerializer(IssueItem.serializer()), items)
            prefs(context).edit().putString(KEY_ISSUES, encoded).apply()
        }
    }

    fun loadIssues(context: Context): List<IssueItem> {
        return runCatching {
            val raw = prefs(context).getString(KEY_ISSUES, null) ?: return emptyList()
            json.decodeFromString(ListSerializer(IssueItem.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    fun saveNotifications(context: Context, items: List<NotificationItem>) {
        runCatching {
            val encoded = json.encodeToString(ListSerializer(NotificationItem.serializer()), items)
            prefs(context).edit().putString(KEY_NOTIFICATIONS, encoded).apply()
        }
    }

    fun loadNotifications(context: Context): List<NotificationItem> {
        return runCatching {
            val raw = prefs(context).getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
            json.decodeFromString(ListSerializer(NotificationItem.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    fun saveActions(context: Context, widgetId: String, items: List<WorkflowRunItem>) {
        runCatching {
            val encoded = json.encodeToString(ListSerializer(WorkflowRunItem.serializer()), items)
            prefs(context).edit().putString("$KEY_ACTIONS_PREFIX$widgetId", encoded).apply()
        }
    }

    fun loadActions(context: Context, widgetId: String): List<WorkflowRunItem> {
        return runCatching {
            val raw = prefs(context).getString("$KEY_ACTIONS_PREFIX$widgetId", null)
                ?: return emptyList()
            json.decodeFromString(ListSerializer(WorkflowRunItem.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    fun saveReviewsCount(context: Context, count: Int) {
        prefs(context).edit().putInt(KEY_REVIEWS_COUNT, count).apply()
    }

    fun loadReviewsCount(context: Context): Int {
        return prefs(context).getInt(KEY_REVIEWS_COUNT, -1)
    }
}
