package cn.waijade.nexuswid.data.afdian

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

sealed class AfdianResult {
    data class Success(val earnings: AfdianEarnings) : AfdianResult()
    data class Error(val message: String) : AfdianResult()
}

sealed class AfdianUnreadResult {
    data class Success(val data: AfdianUnreadCount) : AfdianUnreadResult()
    data class Error(val message: String) : AfdianUnreadResult()
}

data class AfdianServiceConfig(
    val cookie: String,
    val apiUrl: String = "https://afdian.com/api/my/dashboard"
)

class AfdianApiService(
    private val httpClient: HttpClient,
    private val json: Json
) {
    private fun normalizeCookie(cookie: String): String {
        return if (cookie.startsWith("auth_token=")) cookie else "auth_token=$cookie"
    }

    private fun HttpRequestBuilder.commonHeaders(cookie: String) {
        header("Content-Type", "application/json")
        header("Cookie", normalizeCookie(cookie))
        header("User-Agent", "Mozilla/5.0 (Windows) AppleWebKit/537.36 Chrome/122.0.0.0")
        header("referer", "https://afdian.com/")
    }

    suspend fun getEarnings(config: AfdianServiceConfig): AfdianResult {
        android.util.Log.d(TAG, "getEarnings: starts with auth_token= ${config.cookie.startsWith("auth_token=")}")
        return runCatching {
            val response = httpClient.get(config.apiUrl) {
                commonHeaders(config.cookie)
            }
            android.util.Log.d(TAG, "HTTP: ${response.status}")
            if (!response.status.isSuccess()) return AfdianResult.Error("HTTP ${response.status}")

            val bodyText = response.bodyAsText()
            android.util.Log.d(TAG, "resp: ${bodyText.take(300)}")
            val result = json.decodeFromString<AfdianDashboardResponse>(bodyText)
            if (result.ec != 200) return AfdianResult.Error(result.em.ifBlank { "错误$result.ec" })

            val s = result.data?.summary
            AfdianResult.Success(AfdianEarnings(
                totalAmount = s?.all_sum_amount?.toDoubleOrNull() ?: 0.0,
                totalCount = s?.all_sponsor_count ?: 0,
                monthlyAmount = s?.month_amount?.toDoubleOrNull() ?: 0.0,
                monthlyCount = s?.month_sponsor_count ?: 0
            ))
        }.getOrElse { e -> android.util.Log.e(TAG, "err: ${e.message}"); AfdianResult.Error(e.message ?: "未知错误") }
    }

    suspend fun getUnreadCount(cookie: String): AfdianUnreadResult {
        android.util.Log.d(TAG, "getUnreadCount")
        return runCatching {
            val response = httpClient.get(CHECK_URL) {
                commonHeaders(cookie)
            }
            if (!response.status.isSuccess()) return AfdianUnreadResult.Error("HTTP ${response.status}")

            val bodyText = response.bodyAsText()
            android.util.Log.d(TAG, "check resp: ${bodyText.take(200)}")
            val result = json.decodeFromString<AfdianCheckResponse>(bodyText)
            if (result.ec != 200) return AfdianUnreadResult.Error(result.em.ifBlank { "错误$result.ec" })

            val d = result.data
            AfdianUnreadResult.Success(AfdianUnreadCount(
                total = d?.unread_message_num ?: 0,
                comment = d?.unread_count?.comment ?: 0,
                like = d?.unread_count?.like ?: 0,
                message = d?.unread_count?.message ?: 0
            ))
        }.getOrElse { e -> android.util.Log.e(TAG, "err: ${e.message}"); AfdianUnreadResult.Error(e.message ?: "未知错误") }
    }

    companion object {
        private const val TAG = "AfdianApiService"
        private const val CHECK_URL = "https://afdian.com/api/my/check"
    }
}
