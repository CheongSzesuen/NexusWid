package cn.waijade.nexuswid.data.github

import android.content.Context
import android.content.SharedPreferences

class GitHubPreferences(context: Context) {
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

    companion object {
        private const val PREFS_NAME = "github_preferences"
        private const val KEY_USERNAME = "github_username"
        private const val KEY_TOKEN = "github_token"
    }
}