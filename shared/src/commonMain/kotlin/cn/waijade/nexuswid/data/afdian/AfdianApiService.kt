package cn.waijade.nexuswid.data.afdian

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

sealed class AfdianResult {
    data class Success(val earnings: AfdianEarnings) : AfdianResult()
    data class Error(val message: String) : AfdianResult()
}

data class AfdianServiceConfig(
    val cookie: String,
    val apiUrl: String = "https://afdian.com/api/my/dashboard"
)

class AfdianApiService(
    private val httpClient: HttpClient,
    private val json: Json
) {
    suspend fun getEarnings(config: AfdianServiceConfig): AfdianResult {
        val cookie = config.cookie
        val normalizedCookie = if (cookie.startsWith("auth_token=")) cookie
                               else "auth_token=$cookie"
        android.util.Log.d(TAG, "Cookie normalized: starts with auth_token= ${normalizedCookie.startsWith("auth_token=")}")

        return runCatching {
            val response = httpClient.get(config.apiUrl) {
                header("Content-Type", "application/json")
                header("Cookie", normalizedCookie)
                header("User-Agent", "Mozilla/5.0 (Windows) AppleWebKit/537.36 Chrome/122.0.0.0")
                header("referer", "https://afdian.com/")
            }

            android.util.Log.d(TAG, "HTTP status: ${response.status}")

            if (!response.status.isSuccess()) {
                return AfdianResult.Error("HTTP ${response.status}")
            }

            val bodyText = response.bodyAsText()
            android.util.Log.d(TAG, "Response (first 500): ${bodyText.take(500)}")
            val result = json.decodeFromString<AfdianDashboardResponse>(bodyText)

            if (result.ec != 200) {
                android.util.Log.e(TAG, "API ec=${result.ec} em=${result.em}")
                return AfdianResult.Error(result.em.ifBlank { "错误 $result.ec" })
            }

            val summary = result.data?.summary
            val totalAmount = summary?.all_sum_amount?.toDoubleOrNull() ?: 0.0
            val totalCount = summary?.all_sponsor_count ?: 0
            val monthAmount = summary?.month_amount?.toDoubleOrNull() ?: 0.0
            val monthCount = summary?.month_sponsor_count ?: 0

            android.util.Log.d(TAG, "total=$totalAmount($totalCount) month=$monthAmount($monthCount)")
            AfdianResult.Success(AfdianEarnings(totalAmount, totalCount, monthAmount, monthCount))
        }.getOrElse { e ->
            android.util.Log.e(TAG, "Exception: ${e.message}")
            AfdianResult.Error(e.message ?: "未知错误")
        }
    }

    companion object {
        private const val TAG = "AfdianApiService"
    }
}
