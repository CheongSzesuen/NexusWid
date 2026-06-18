package cn.waijade.nexuswid.data.github

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

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
