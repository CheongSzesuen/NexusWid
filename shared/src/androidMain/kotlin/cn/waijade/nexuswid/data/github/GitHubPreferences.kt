package cn.waijade.nexuswid.data.github

import android.content.Context
import android.content.SharedPreferences
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.WidgetPreferences
import cn.waijade.nexuswid.data.github.IssueType

class GitHubPreferences(context: Context) : WidgetPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    var username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var token: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    val isConfigured: Boolean
        get() = username.isNotBlank()

    override var selectedPullRequestTypes: Set<PullRequestType>
        get() {
            val typeNames = prefs.getStringSet(KEY_PULL_REQUEST_TYPES, DEFAULT_PULL_REQUEST_TYPES)
                ?: DEFAULT_PULL_REQUEST_TYPES
            return typeNames.mapNotNull { name ->
                try {
                    PullRequestType.valueOf(name)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }.toSet()
        }
        set(value) {
            prefs.edit().putStringSet(KEY_PULL_REQUEST_TYPES, value.map { it.name }.toSet()).commit()
        }

    override var selectedIssueTypes: Set<IssueType>
        get() {
            val typeNames = prefs.getStringSet(KEY_ISSUE_TYPES, DEFAULT_ISSUE_TYPES)
                ?: DEFAULT_ISSUE_TYPES
            return typeNames.mapNotNull { name ->
                try {
                    IssueType.valueOf(name)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }.toSet()
        }
        set(value) {
            prefs.edit().putStringSet(KEY_ISSUE_TYPES, value.map { it.name }.toSet()).commit()
        }

    override var weekStartsOnMonday: Boolean
        get() = prefs.getBoolean(KEY_WEEK_STARTS_ON_MONDAY, false)
        set(value) {
            prefs.edit().putBoolean(KEY_WEEK_STARTS_ON_MONDAY, value).commit()
        }

    override var widgetHeatmapColorMode: HeatmapColorMode
        get() {
            val name = prefs.getString(KEY_WIDGET_HEATMAP_COLOR_MODE, DEFAULT_WIDGET_HEATMAP_COLOR_MODE)
                ?: DEFAULT_WIDGET_HEATMAP_COLOR_MODE
            return try {
                HeatmapColorMode.valueOf(name)
            } catch (_: IllegalArgumentException) {
                HeatmapColorMode.SYSTEM
            }
        }
        set(value) {
            prefs.edit().putString(KEY_WIDGET_HEATMAP_COLOR_MODE, value.name).commit()
        }

    override var widgetColorMode: HeatmapColorMode
        get() {
            val name = prefs.getString(KEY_WIDGET_COLOR_MODE, DEFAULT_WIDGET_COLOR_MODE)
                ?: DEFAULT_WIDGET_COLOR_MODE
            return try {
                HeatmapColorMode.valueOf(name)
            } catch (_: IllegalArgumentException) {
                HeatmapColorMode.SYSTEM
            }
        }
        set(value) {
            prefs.edit().putString(KEY_WIDGET_COLOR_MODE, value.name).commit()
        }

    override var liquidGlassBottomBar: Boolean
        get() = prefs.getBoolean(KEY_LIQUID_GLASS_BOTTOM_BAR, false)
        set(value) {
            prefs.edit().putBoolean(KEY_LIQUID_GLASS_BOTTOM_BAR, value).commit()
        }

    var debugCountTextScale: Float
        get() = prefs.getFloat(KEY_DEBUG_COUNT_TEXT_SCALE, 1.0f)
        set(value) {
            prefs.edit().putFloat(KEY_DEBUG_COUNT_TEXT_SCALE, value).commit()
        }

    var debugCountValue: Int
        get() = prefs.getInt(KEY_DEBUG_COUNT_VALUE, -1)
        set(value) {
            prefs.edit().putInt(KEY_DEBUG_COUNT_VALUE, value).commit()
        }

    var debugUseTestData: Boolean
        get() = prefs.getBoolean(KEY_DEBUG_USE_TEST_DATA, false)
        set(value) {
            prefs.edit().putBoolean(KEY_DEBUG_USE_TEST_DATA, value).commit()
        }

    var actionsRepo: String
        get() = prefs.getString(KEY_ACTIONS_REPO, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_ACTIONS_REPO, value).commit()
        }

    companion object {
        private const val PREFS_NAME = "github_preferences"
        private const val KEY_USERNAME = "github_username"
        private const val KEY_TOKEN = "github_token"
        private const val KEY_WEEK_STARTS_ON_MONDAY = "week_starts_on_monday"
        private const val KEY_WIDGET_HEATMAP_COLOR_MODE = "widget_heatmap_color_mode"
        private const val KEY_WIDGET_COLOR_MODE = "widget_color_mode"
        private const val KEY_LIQUID_GLASS_BOTTOM_BAR = "liquid_glass_bottom_bar"
        private const val KEY_PULL_REQUEST_TYPES = "pull_request_types"
        private const val KEY_ISSUE_TYPES = "issue_types"
        private const val KEY_DEBUG_COUNT_TEXT_SCALE = "debug_count_text_scale"
        private const val KEY_DEBUG_COUNT_VALUE = "debug_count_value"
        private const val KEY_DEBUG_USE_TEST_DATA = "debug_use_test_data"
        private const val KEY_ACTIONS_REPO = "actions_repo"
        private const val DEFAULT_WIDGET_HEATMAP_COLOR_MODE = "SYSTEM"
        private const val DEFAULT_WIDGET_COLOR_MODE = "SYSTEM"
        private val DEFAULT_PULL_REQUEST_TYPES = setOf(
            PullRequestType.REVIEW_REQUESTED.name
        )
        private val DEFAULT_ISSUE_TYPES = setOf(
            IssueType.ASSIGNED.name
        )
    }
}
