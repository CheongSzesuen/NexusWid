package cn.waijade.nexuswid.data.github

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GitHubApiService(
    private val httpClient: HttpClient,
    private val json: Json
) {
    suspend fun getContributionData(
        username: String,
        token: String? = null
    ): Result<List<ContributionDay>> = runCatching {
        val url = "https://github.com/users/$username/contributions"
        println("GitHubApiService: fetching $url")
        
        val response = httpClient.get(url) {
            header("User-Agent", "NexusWid/1.0")
            token?.takeIf { it.isNotBlank() }?.let {
                header("Authorization", "bearer $it")
            }
        }

        println("GitHubApiService: response status=${response.status}")

        if (!response.status.isSuccess()) {
            throw Exception("GitHub API error: ${response.status}")
        }

        val html = response.bodyAsText()
        println("GitHubApiService: html length=${html.length}")
        
        val days = parseContributionHtml(html)
        println("GitHubApiService: parsed ${days.size} days")
        
        days
    }

    suspend fun getPullRequestCount(
        token: String,
        pullRequestTypes: Set<PullRequestType>
    ): Result<Int> = runCatching {
        val typeQuery = pullRequestTypes.joinToString("+") { it.query }
        val url = "https://api.github.com/search/issues?q=is:pr+is:open+$typeQuery&per_page=1"
        println("GitHubApiService: fetching pull request count with types: ${pullRequestTypes.map { it.name }}")

        val response = httpClient.get(url) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }

        println("GitHubApiService: pull request response status=${response.status}")

        if (!response.status.isSuccess()) {
            throw Exception("GitHub API error: ${response.status}")
        }

        val bodyText = response.bodyAsText()
        val jsonElement = json.parseToJsonElement(bodyText)
        val totalCount = jsonElement.jsonObject["total_count"]?.jsonPrimitive?.int ?: 0
        println("GitHubApiService: pull request count=$totalCount")

        totalCount
    }

    suspend fun getPullRequestList(
        token: String,
        pullRequestTypes: Set<PullRequestType>,
        limit: Int = 20,
        withCheckStatus: Int = 0
    ): Result<List<PullRequestItem>> = runCatching {
        if (pullRequestTypes.isEmpty()) return@runCatching emptyList()
        val typeQuery = pullRequestTypes.joinToString("+") { it.query }
        val url =
            "https://api.github.com/search/issues?q=is:pr+is:open+$typeQuery&sort=updated&per_page=$limit"
        println("GitHubApiService: fetching pull request list types=${pullRequestTypes.map { it.name }} limit=$limit")

        val response = httpClient.get(url) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }

        if (!response.status.isSuccess()) {
            throw Exception("GitHub API error: ${response.status}")
        }

        val bodyText = response.bodyAsText()
        val items = json.parseToJsonElement(bodyText).jsonObject["items"]?.jsonArray
            ?: return@runCatching emptyList()

        val rawList = items.mapNotNull { item ->
            val obj = item.jsonObject
            val number = obj["number"]?.jsonPrimitive?.int ?: return@mapNotNull null
            val title = obj["title"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val htmlUrl = obj["html_url"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val pullsApiUrl = obj["pull_request"]?.jsonObject?.get("url")
                ?.jsonPrimitive?.content ?: return@mapNotNull null
            val repoFullName = extractRepoFullName(htmlUrl) ?: return@mapNotNull null
            PullRequestItem(
                repoFullName = repoFullName,
                number = number,
                title = title,
                htmlUrl = htmlUrl,
                pullsApiUrl = pullsApiUrl,
                checkStatus = CheckStatus.NONE
            )
        }

        if (withCheckStatus <= 0) return@runCatching rawList

        val needStatus = rawList.take(withCheckStatus)
        val rest = rawList.drop(withCheckStatus)

        val withStatus = coroutineScope {
            needStatus.map { pr ->
                async {
                    val status = runCatching { fetchCombinedStatus(token, pr.pullsApiUrl) }
                        .getOrElse { CheckStatus.NONE }
                    pr.copy(checkStatus = status)
                }
            }.awaitAll()
        }

        withStatus + rest
    }

    private suspend fun fetchCombinedStatus(token: String, pullsApiUrl: String): CheckStatus {
        val prResp = httpClient.get(pullsApiUrl) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }
        if (!prResp.status.isSuccess()) return CheckStatus.NONE
        val prObj = json.parseToJsonElement(prResp.bodyAsText()).jsonObject
        val statusesUrl = prObj["statuses_url"]?.jsonPrimitive?.content ?: return CheckStatus.NONE

        val baseUrl = statusesUrl.substringBeforeLast("/statuses/")
        val sha = statusesUrl.substringAfterLast("/")
        val combinedUrl = "$baseUrl/commits/$sha/status"

        val combinedResp = httpClient.get(combinedUrl) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }
        if (!combinedResp.status.isSuccess()) return CheckStatus.NONE

        val combinedObj = json.parseToJsonElement(combinedResp.bodyAsText()).jsonObject
        val state = combinedObj["state"]?.jsonPrimitive?.content ?: return CheckStatus.NONE
        val totalCount = combinedObj["total_count"]?.jsonPrimitive?.int ?: 0

        return when {
            totalCount == 0 -> CheckStatus.NONE
            state == "success" -> CheckStatus.SUCCESS
            state == "failure" || state == "error" -> CheckStatus.FAILURE
            else -> CheckStatus.PENDING
        }
    }

    private fun extractRepoFullName(htmlUrl: String): String? {
        val parts = htmlUrl.removePrefix("https://github.com/").split("/")
        if (parts.size < 2) return null
        return "${parts[0]}/${parts[1]}"
    }

    private fun parseContributionHtml(html: String): List<ContributionDay> {
        val days = mutableListOf<ContributionDay>()
        
        // GitHub 使用 <td> 标签而不是 <rect> 标签
        val tdPattern = """<td[^>]*data-date="([^"]*)"[^>]*data-level="(\d+)"[^>]*>""".toRegex()
        
        tdPattern.findAll(html).forEach { match ->
            val date = match.groupValues[1]
            val level = match.groupValues[2].toIntOrNull() ?: 0
            days.add(ContributionDay(
                contributionCount = level,  // 使用 level 作为 count
                date = date,
                color = level.toString()
            ))
        }
        
        return days
    }
}
