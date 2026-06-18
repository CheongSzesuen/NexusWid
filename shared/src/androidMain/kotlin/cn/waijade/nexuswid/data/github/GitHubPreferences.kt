package cn.waijade.nexuswid.data.github

import android.content.Context
import android.content.SharedPreferences
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.WidgetPreferences

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

    companion object {
        private const val PREFS_NAME = "github_preferences"
        private const val KEY_USERNAME = "github_username"
        private const val KEY_TOKEN = "github_token"
        private const val KEY_WEEK_STARTS_ON_MONDAY = "week_starts_on_monday"
        private const val KEY_WIDGET_HEATMAP_COLOR_MODE = "widget_heatmap_color_mode"
        private const val DEFAULT_WIDGET_HEATMAP_COLOR_MODE = "SYSTEM"
    }
}
