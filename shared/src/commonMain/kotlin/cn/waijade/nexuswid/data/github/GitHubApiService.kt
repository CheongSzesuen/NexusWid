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

    suspend fun getIssueList(
        token: String,
        issueTypes: Set<IssueType>,
        limit: Int = 20
    ): Result<List<IssueItem>> = runCatching {
        if (issueTypes.isEmpty()) return@runCatching emptyList()
        val typeQuery = issueTypes.joinToString("+") { it.query }
        val url = "https://api.github.com/search/issues?q=is:open+is:issue+$typeQuery&sort=updated&per_page=$limit"

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

        items.mapNotNull { item ->
            val obj = item.jsonObject
            val number = obj["number"]?.jsonPrimitive?.int ?: return@mapNotNull null
            val title = obj["title"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val htmlUrl = obj["html_url"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val state = obj["state"]?.jsonPrimitive?.content ?: "open"
            val repoFullName = extractRepoFullName(htmlUrl) ?: return@mapNotNull null
            IssueItem(
                repoFullName = repoFullName,
                number = number,
                title = title,
                htmlUrl = htmlUrl,
                state = state
            )
        }
    }

    suspend fun getWorkflowRuns(
        token: String,
        repo: String,
        limit: Int = 10
    ): Result<List<WorkflowRunItem>> = runCatching {
        val url = "https://api.github.com/repos/$repo/actions/runs?per_page=$limit"
        println("GitHubApiService: fetching workflow runs for $repo")

        val response = httpClient.get(url) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }

        if (!response.status.isSuccess()) {
            throw Exception("GitHub API error: ${response.status}")
        }

        val bodyText = response.bodyAsText()
        val runs = json.parseToJsonElement(bodyText).jsonObject["workflow_runs"]?.jsonArray
            ?: return@runCatching emptyList()

        runs.mapNotNull { item ->
            val obj = item.jsonObject
            val workflowName = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val branch = obj["head_branch"]?.jsonPrimitive?.content ?: ""
            val commitObj = obj["head_commit"]?.jsonObject
            val commitMessage = commitObj?.get("message")?.jsonPrimitive?.content ?: ""
            val status = obj["status"]?.jsonPrimitive?.content ?: "unknown"
            val conclusion = obj["conclusion"]?.jsonPrimitive?.content
            val htmlUrl = obj["html_url"]?.jsonPrimitive?.content ?: ""
            val event = obj["event"]?.jsonPrimitive?.content ?: ""
            val updatedAt = obj["updated_at"]?.jsonPrimitive?.content
            val runNumber = obj["run_number"]?.jsonPrimitive?.int ?: 0
            val actor = obj["actor"]?.jsonObject?.get("login")?.jsonPrimitive?.content
            val runStartedAt = obj["run_started_at"]?.jsonPrimitive?.content
            WorkflowRunItem(
                workflowName = workflowName,
                branch = branch,
                commitMessage = commitMessage,
                status = status,
                conclusion = conclusion,
                htmlUrl = htmlUrl,
                event = event,
                updatedAt = updatedAt,
                runNumber = runNumber,
                actor = actor,
                runStartedAt = runStartedAt
            )
        }
    }

    suspend fun getUserRepos(
        token: String,
        limit: Int = 100
    ): Result<List<RepoItem>> = runCatching {
        val url = "https://api.github.com/user/repos?per_page=$limit&sort=updated&type=owner"
        println("GitHubApiService: fetching user repos")

        val response = httpClient.get(url) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }

        if (!response.status.isSuccess()) {
            throw Exception("GitHub API error: ${response.status}")
        }

        val jsonArray = json.parseToJsonElement(response.bodyAsText()).jsonArray
        jsonArray.mapNotNull { item ->
            val obj = item.jsonObject
            val fullName = obj["full_name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val ownerLogin = obj["owner"]?.jsonObject?.get("login")?.jsonPrimitive?.content ?: ""
            val description = obj["description"]?.jsonPrimitive?.content
            val language = obj["language"]?.jsonPrimitive?.content
            val updatedAt = obj["updated_at"]?.jsonPrimitive?.content ?: ""
            val isPrivate = obj["private"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
            RepoItem(
                fullName = fullName,
                name = name,
                ownerLogin = ownerLogin,
                description = description,
                language = language,
                updatedAt = updatedAt,
                isPrivate = isPrivate
            )
        }
    }

    suspend fun searchRepos(
        token: String,
        query: String,
        limit: Int = 30
    ): Result<List<RepoItem>> = runCatching {
        val safeQuery = query.replace(" ", "+").replace("#", "%23")
        val url = "https://api.github.com/search/repositories?q=$safeQuery&per_page=$limit&sort=stars"
        println("GitHubApiService: searching repos for '$query'")

        val response = httpClient.get(url) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }

        if (!response.status.isSuccess()) {
            throw Exception("GitHub API error: ${response.status}")
        }

        val items = json.parseToJsonElement(response.bodyAsText()).jsonObject["items"]?.jsonArray
            ?: return@runCatching emptyList()

        items.mapNotNull { item ->
            val obj = item.jsonObject
            val fullName = obj["full_name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val ownerLogin = obj["owner"]?.jsonObject?.get("login")?.jsonPrimitive?.content ?: ""
            val description = obj["description"]?.jsonPrimitive?.content
            val language = obj["language"]?.jsonPrimitive?.content
            val updatedAt = obj["updated_at"]?.jsonPrimitive?.content ?: ""
            val isPrivate = obj["private"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
            RepoItem(
                fullName = fullName,
                name = name,
                ownerLogin = ownerLogin,
                description = description,
                language = language,
                updatedAt = updatedAt,
                isPrivate = isPrivate
            )
        }
    }

    suspend fun getWorkflowsWithLatestRun(
        token: String,
        repo: String
    ): Result<List<WorkflowSummary>> = runCatching {
        val workflowsUrl = "https://api.github.com/repos/$repo/actions/workflows?per_page=50"
        println("GitHubApiService: fetching workflows for $repo")

        val wfResponse = httpClient.get(workflowsUrl) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }
        if (!wfResponse.status.isSuccess()) {
            throw Exception("GitHub API error: ${wfResponse.status}")
        }

        val workflows = json.parseToJsonElement(wfResponse.bodyAsText())
            .jsonObject["workflows"]?.jsonArray ?: return@runCatching emptyList()

        workflows.mapNotNull { item ->
            val obj = item.jsonObject
            val id = obj["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: return@mapNotNull null
            val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val path = obj["path"]?.jsonPrimitive?.content ?: ""
            val state = obj["state"]?.jsonPrimitive?.content ?: ""

            val runsUrl = "https://api.github.com/repos/$repo/actions/workflows/$id/runs?per_page=1"
            val runResp = runCatching {
                httpClient.get(runsUrl) {
                    header("User-Agent", "NexusWid/1.0")
                    header("Authorization", "bearer $token")
                    header("Accept", "application/vnd.github+json")
                }
            }
            val runObj = runResp.getOrNull()?.let { resp ->
                if (!resp.status.isSuccess()) null
                else json.parseToJsonElement(resp.bodyAsText())
                    .jsonObject["workflow_runs"]?.jsonArray?.firstOrNull()?.jsonObject
            }

            WorkflowSummary(
                id = id,
                name = name,
                path = path,
                state = state,
                status = runObj?.get("status")?.jsonPrimitive?.content,
                conclusion = runObj?.get("conclusion")?.jsonPrimitive?.content,
                branch = runObj?.get("head_branch")?.jsonPrimitive?.content,
                updatedAt = runObj?.get("updated_at")?.jsonPrimitive?.content
            )
        }
    }

    private fun extractRepoFullName(htmlUrl: String): String? {
        val parts = htmlUrl.removePrefix("https://github.com/").split("/")
        if (parts.size < 2) return null
        return "${parts[0]}/${parts[1]}"
    }

    suspend fun getNotifications(
        token: String,
        limit: Int = 20,
        participating: Boolean = false
    ): Result<List<NotificationItem>> = runCatching {
        val url = buildString {
            append("https://api.github.com/notifications?per_page=$limit")
            if (participating) append("&participating=true")
        }
        println("GitHubApiService: fetching notifications limit=$limit participating=$participating")

        val response = httpClient.get(url) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }

        if (!response.status.isSuccess()) {
            throw Exception("GitHub API error: ${response.status}")
        }

        val bodyText = response.bodyAsText()
        val threads = json.parseToJsonElement(bodyText).jsonArray

        val rawItems = threads.mapNotNull { thread ->
            val obj = thread.jsonObject
            val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val unread = obj["unread"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
            val updatedAt = obj["updated_at"]?.jsonPrimitive?.content ?: ""

            val repoObj = obj["repository"]?.jsonObject
            val repoFullName = repoObj?.get("full_name")?.jsonPrimitive?.content ?: "unknown"

            val subjectObj = obj["subject"]?.jsonObject
            val title = subjectObj?.get("title")?.jsonPrimitive?.content ?: ""
            val subjectTypeStr = subjectObj?.get("type")?.jsonPrimitive?.content ?: ""
            val subjectUrl = subjectObj?.get("url")?.jsonPrimitive?.content ?: ""

            val reasonStr = obj["reason"]?.jsonPrimitive?.content ?: ""

            val subjectType = parseSubjectType(subjectTypeStr)
            val reason = parseReason(reasonStr)

            NotificationItem(
                id = id,
                repoFullName = repoFullName,
                title = title,
                subjectType = subjectType,
                reason = reason,
                isUnread = unread,
                updatedAt = updatedAt,
                htmlUrl = "",
                subjectApiUrl = subjectUrl,
                state = null
            )
        }

        val needState = rawItems.take(limit)
        val withState = coroutineScope {
            needState.map { item ->
                async {
                    if (item.subjectApiUrl.isBlank() || item.subjectType == NotificationSubjectType.OTHER) {
                        return@async item
                    }
                    val state = runCatching { fetchSubjectState(token, item.subjectApiUrl) }
                        .getOrNull()
                    item.copy(state = state, htmlUrl = buildNotificationHtmlUrl(item.subjectApiUrl, item.subjectType, state))
                }
            }.awaitAll()
        }

        withState
    }

    private suspend fun fetchSubjectState(token: String, apiUrl: String): String? {
        val response = httpClient.get(apiUrl) {
            header("User-Agent", "NexusWid/1.0")
            header("Authorization", "bearer $token")
            header("Accept", "application/vnd.github+json")
        }
        if (!response.status.isSuccess()) return null
        val obj = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val state = obj["state"]?.jsonPrimitive?.content
        val merged = obj["merged"]?.jsonPrimitive?.content?.toBooleanStrictOrNull()
        return if (merged == true) "merged" else state
    }

    private fun parseSubjectType(type: String): NotificationSubjectType {
        return when (type) {
            "Issue" -> NotificationSubjectType.ISSUE
            "PullRequest" -> NotificationSubjectType.PULL_REQUEST
            "Commit" -> NotificationSubjectType.COMMIT
            "Release" -> NotificationSubjectType.RELEASE
            "Discussion" -> NotificationSubjectType.DISCUSSION
            "CheckSuite" -> NotificationSubjectType.CHECK_SUITE
            else -> NotificationSubjectType.OTHER
        }
    }

    private fun parseReason(reason: String): NotificationReason {
        return when (reason) {
            "assign" -> NotificationReason.ASSIGN
            "author" -> NotificationReason.AUTHOR
            "comment" -> NotificationReason.COMMENT
            "ci_activity" -> NotificationReason.CI_ACTIVITY
            "invitation" -> NotificationReason.INVITATION
            "manual" -> NotificationReason.MANUAL
            "mention" -> NotificationReason.MENTION
            "review_requested" -> NotificationReason.REVIEW_REQUESTED
            "security_alert" -> NotificationReason.SECURITY_ALERT
            "state_change" -> NotificationReason.STATE_CHANGE
            "subscribed" -> NotificationReason.SUBSCRIBED
            "team_mention" -> NotificationReason.TEAM_MENTION
            "approval_requested" -> NotificationReason.APPROVAL_REQUESTED
            "member_feature_requested" -> NotificationReason.MEMBER_FEATURE_REQUESTED
            "security_advisory_credit" -> NotificationReason.SECURITY_ADVISORY_CREDIT
            else -> NotificationReason.SUBSCRIBED
        }
    }

    private fun buildNotificationHtmlUrl(apiUrl: String, subjectType: NotificationSubjectType, state: String? = null): String {
        if (!apiUrl.startsWith("https://api.github.com/")) return ""
        val path = apiUrl.removePrefix("https://api.github.com/")
        return when (subjectType) {
            NotificationSubjectType.ISSUE -> {
                val parts = path.split("/")
                if (parts.size >= 4) "https://github.com/${parts[1]}/${parts[2]}/issues/${parts[4]}" else ""
            }
            NotificationSubjectType.PULL_REQUEST -> {
                val parts = path.split("/")
                if (parts.size >= 4) "https://github.com/${parts[1]}/${parts[2]}/pull/${parts[4]}" else ""
            }
            NotificationSubjectType.RELEASE -> {
                val parts = path.split("/")
                if (parts.size >= 4) "https://github.com/${parts[1]}/${parts[2]}/releases/tag/${parts[5]}" else ""
            }
            else -> ""
        }
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
