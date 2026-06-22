package cn.waijade.nexuswid.data.afdian

import android.content.Context
import android.content.SharedPreferences

class AfdianPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    var cookie: String
        get() = prefs.getString(KEY_COOKIE, "") ?: ""
        set(value) {
            val normalized = if (value.isBlank() || value.startsWith("auth_token=")) value
                             else "auth_token=$value"
            prefs.edit().putString(KEY_COOKIE, normalized).apply()
        }

    val isConfigured: Boolean
        get() = cookie.isNotBlank()

    var selectedProductPlanId: String
        get() = prefs.getString(KEY_PRODUCT_PLAN_ID, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_PRODUCT_PLAN_ID, value).apply()
        }

    var userId: String
        get() = prefs.getString(KEY_USER_ID, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_USER_ID, value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "afdian_preferences"
        private const val KEY_COOKIE = "afdian_cookie"
        private const val KEY_PRODUCT_PLAN_ID = "selected_product_plan_id"
        private const val KEY_USER_ID = "afdian_user_id"
    }
}