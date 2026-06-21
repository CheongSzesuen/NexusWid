package cn.waijade.nexuswid.data.afdian

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json

sealed class AfdianResult {
    data class Success(val earnings: AfdianEarnings) : AfdianResult()
    data class Error(val message: String) : AfdianResult()
}

sealed class AfdianUnreadResult {
    data class Success(val data: AfdianUnreadCount) : AfdianUnreadResult()
    data class Error(val message: String) : AfdianUnreadResult()
}

sealed class AfdianDialogsResult {
    data class Success(val data: AfdianDialogsData) : AfdianDialogsResult()
    data class Error(val message: String) : AfdianDialogsResult()
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

    suspend fun getPlans(cookie: String): List<AfdianPlan> {
        android.util.Log.d(TAG, "getPlans")
        return runCatching {
            val response = httpClient.get(PLANS_URL) {
                commonHeaders(cookie)
            }
            if (!response.status.isSuccess()) return emptyList()

            val bodyText = response.bodyAsText()
            android.util.Log.d(TAG, "plans resp: ${bodyText.take(200)}")
            val result = json.decodeFromString<AfdianPlanResponse>(bodyText)
            if (result.ec != 200) return emptyList()

            val allPlans = mutableListOf<AfdianPlan>()
            result.data?.list?.let { allPlans.addAll(it) }
            result.data?.sale_list?.let { allPlans.addAll(it) }
            allPlans.distinctBy { it.plan_id }
                .filter { it.status == 1 }
        }.getOrElse { e ->
            android.util.Log.e(TAG, "err: ${e.message}")
            emptyList()
        }
    }

    suspend fun getDailyStats(cookie: String): List<AfdianDailyStat> {
        android.util.Log.d(TAG, "getDailyStats")
        val now = Clock.System.now()
        val actionDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val currentMonthPrefix = "${actionDate.year}${actionDate.monthNumber.toString().padStart(2, '0')}"

        val allStats = mutableListOf<AfdianDailyStat>()
        var page = 1
        while (true) {
            val response = httpClient.get(STAT_URL) {
                commonHeaders(cookie)
                parameter("page", page)
                parameter("type", "day")
            }
            if (!response.status.isSuccess()) break

            val bodyText = response.bodyAsText()
            val result = json.decodeFromString<AfdianStatResponse>(bodyText)
            if (result.ec != 200) break

            val data = result.data ?: break
            val list = data.list
            if (list.isEmpty()) break

            val currentMonthItems = list.filter { it.date_str.toString().startsWith(currentMonthPrefix) }
            allStats.addAll(currentMonthItems)

            if (list.last().date_str.toString() < currentMonthPrefix || data.has_more == 0) break
            page++
        }

        return allStats.sortedBy { it.date_str }
    }

    suspend fun getDialogs(cookie: String, page: Int = 1): AfdianDialogsResult {
        android.util.Log.d(TAG, "getDialogs page=$page")
        return runCatching {
            val response = httpClient.get(DIALOGS_URL) {
                commonHeaders(cookie)
                parameter("page", page)
                parameter("unread", 0)
            }
            if (!response.status.isSuccess()) return AfdianDialogsResult.Error("HTTP ${response.status}")

            val bodyText = response.bodyAsText()
            android.util.Log.d(TAG, "dialogs resp: ${bodyText.take(200)}")
            val result = json.decodeFromString<AfdianDialogsResponse>(bodyText)
            if (result.ec != 200) return AfdianDialogsResult.Error(result.em.ifBlank { "错误$result.ec" })

            AfdianDialogsResult.Success(result.data ?: AfdianDialogsData())
        }.getOrElse { e -> android.util.Log.e(TAG, "err: ${e.message}"); AfdianDialogsResult.Error(e.message ?: "未知错误") }
    }

    suspend fun getComplaintCount(cookie: String): Int {
        android.util.Log.d(TAG, "getComplaintCount")
        val complaintKeys = mutableSetOf<String>()
        val maxPages = 5
        
        for (page in 1..maxPages) {
            when (val result = getDialogs(cookie, page)) {
                is AfdianDialogsResult.Success -> {
                    val dialogs = result.data.list
                    for (dialog in dialogs) {
                        if (dialog.unread_count > 0 && 
                            dialog.desc.contains("发起投诉") &&
                            dialog.desc.contains("https://ifdian.net/u/")) {
                            val userId = dialog.user?.user_id ?: continue
                            val orderId = extractOrderId(dialog.desc)
                            if (orderId != null) {
                                complaintKeys.add("${userId}_$orderId")
                            }
                        }
                    }
                    
                    if (result.data.has_more == 0 || page >= result.data.total_page) {
                        break
                    }
                }
                is AfdianDialogsResult.Error -> {
                    android.util.Log.e(TAG, "Failed to get dialogs: ${result.message}")
                    break
                }
            }
        }
        
        android.util.Log.d(TAG, "Total complaint count: ${complaintKeys.size}")
        return complaintKeys.size
    }

    private fun extractOrderId(desc: String): String? {
        val match = Regex("""如超期可能会影响你正常收款[：:]\s*\n?(\d{20,})""").find(desc)
        return match?.groupValues?.get(1)
    }

    companion object {
        private const val TAG = "AfdianApiService"
        private const val CHECK_URL = "https://afdian.com/api/my/check"
        private const val PLANS_URL = "https://afdian.com/api/creator/all-plans"
        private const val STAT_URL = "https://afdian.com/api/my/stat"
        private const val DIALOGS_URL = "https://afdian.com/api/message/dialogs"
    }
}
