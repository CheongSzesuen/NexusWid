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

    suspend fun getMonthlyIncome(cookie: String): List<AfdianMonthlyIncome> {
        android.util.Log.d(TAG, "getMonthlyIncome")
        return runCatching {
            val response = httpClient.get(INCOME_URL) {
                commonHeaders(cookie)
            }
            if (!response.status.isSuccess()) return emptyList()

            val bodyText = response.bodyAsText()
            android.util.Log.d(TAG, "income resp: ${bodyText.take(200)}")
            val result = json.decodeFromString<AfdianIncomeResponse>(bodyText)
            if (result.ec != 200) return emptyList()

            val monthlyBills = result.data?.monthly_bill ?: return emptyList()
            val incomes = mutableListOf<AfdianMonthlyIncome>()
            
            for (yearlyBill in monthlyBills) {
                for (monthlyBill in yearlyBill.data) {
                    incomes.add(
                        AfdianMonthlyIncome(
                            year = yearlyBill.year,
                            month = monthlyBill.month,
                            totalAmount = monthlyBill.data.total_amount.toDoubleOrNull() ?: 0.0,
                            creatorAmount = monthlyBill.data.creator_amount.toDoubleOrNull() ?: 0.0,
                            sponsorCount = monthlyBill.data.sponsor_count
                        )
                    )
                }
            }
            
            incomes.sortedWith(compareByDescending<AfdianMonthlyIncome> { it.year }.thenByDescending { it.month })
        }.getOrElse { e ->
            android.util.Log.e(TAG, "err: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRandomCreator(cookie: String): AfdianRandomCreator? {
        android.util.Log.d(TAG, "getRandomCreator")
        return runCatching {
            val randomPage = (1..30).random()
            val url = "$CREATOR_LIST_URL?page=$randomPage&category_id=&q="
            android.util.Log.d(TAG, "creator url: $url")
            
            val response = httpClient.get(url) {
                commonHeaders(cookie)
            }
            if (!response.status.isSuccess()) return null

            val bodyText = response.bodyAsText()
            android.util.Log.d(TAG, "creator resp: ${bodyText.take(300)}")
            val result = json.decodeFromString<AfdianCreatorListResponse>(bodyText)
            if (result.ec != 200) return null

            val creators = result.data?.list ?: return null
            if (creators.isEmpty()) return null

            val item = creators.random()
            AfdianRandomCreator(
                userId = item.user_id,
                name = item.name,
                avatar = item.avatar,
                urlSlug = item.url_slug,
                isVerified = item.is_verified == 1,
                doing = item.creator?.doing ?: "",
                categoryName = item.creator?.category?.name ?: ""
            )
        }.getOrElse { e ->
            android.util.Log.e(TAG, "getRandomCreator err: ${e.message}")
            null
        }
    }

    suspend fun getTopSponsors(userId: String): List<AfdianTopSponsor> {
        android.util.Log.d(TAG, "getTopSponsors for userId=$userId")
        return runCatching {
            val url = "$TOP_SPONSORS_URL?user_id=$userId"
            val response = httpClient.get(url)
            if (!response.status.isSuccess()) return emptyList()

            val bodyText = response.bodyAsText()
            android.util.Log.d(TAG, "topSponsors resp: ${bodyText.take(300)}")
            val result = json.decodeFromString<AfdianTopSponsorsResponse>(bodyText)
            if (result.ec != 200) return emptyList()

            result.data?.list?.map { item ->
                AfdianTopSponsor(
                    userId = item.user_id,
                    name = item.name.ifBlank { "匿名用户" },
                    avatar = item.avatar,
                    urlSlug = item.url_slug,
                    isVerified = item.is_verified == 1
                )
            } ?: emptyList()
        }.getOrElse { e ->
            android.util.Log.e(TAG, "getTopSponsors err: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserIdFromProfile(cookie: String): String? {
        android.util.Log.d(TAG, "getUserIdFromProfile")
        return runCatching {
            val response = httpClient.get(PROFILE_URL) {
                commonHeaders(cookie)
            }
            if (!response.status.isSuccess()) return null

            val bodyText = response.bodyAsText()
            android.util.Log.d(TAG, "profile resp: ${bodyText.take(200)}")
            val result = json.decodeFromString<AfdianProfileResponse>(bodyText)
            if (result.ec != 200) return null

            result.data?.user?.user_id?.takeIf { it.isNotBlank() }
        }.getOrElse { e ->
            android.util.Log.e(TAG, "getUserIdFromProfile err: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "AfdianApiService"
        private const val CHECK_URL = "https://afdian.com/api/my/check"
        private const val PLANS_URL = "https://afdian.com/api/creator/all-plans"
        private const val STAT_URL = "https://afdian.com/api/my/stat"
        private const val DIALOGS_URL = "https://afdian.com/api/message/dialogs"
        private const val INCOME_URL = "https://afdian.com/api/my/income"
        private const val CREATOR_LIST_URL = "https://ifdian.net/api/creator/list"
        private const val TOP_SPONSORS_URL = "https://afdian.com/api/creator/get-top-sponsors"
        private const val PROFILE_URL = "https://afdian.com/api/my/profile"
    }
}
