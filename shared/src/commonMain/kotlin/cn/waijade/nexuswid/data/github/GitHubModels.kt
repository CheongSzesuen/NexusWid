package cn.waijade.nexuswid.data.github

import kotlinx.serialization.Serializable

@Serializable
data class GitHubGraphQLRequest(
    val query: String,
    val variables: Map<String, String> = emptyMap()
)

@Serializable
data class GitHubGraphQLResponse(
    val data: GitHubData? = null,
    val errors: List<GitHubError>? = null
)

@Serializable
data class GitHubData(
    val user: GitHubUser? = null
)

@Serializable
data class GitHubUser(
    val contributionsCollection: ContributionsCollection
)

@Serializable
data class ContributionsCollection(
    val contributionCalendar: ContributionCalendar
)

@Serializable
data class ContributionCalendar(
    val totalContributions: Int,
    val weeks: List<ContributionWeek>
)

@Serializable
data class ContributionWeek(
    val contributionDays: List<ContributionDay>
)

@Serializable
data class ContributionDay(
    val contributionCount: Int,
    val date: String,
    val color: String
)

@Serializable
data class GitHubError(
    val message: String
)

@Serializable
data class ReviewRequestedSearchResult(
    val total_count: Int
)

enum class PullRequestType(val query: String, val displayName: String) {
    CREATED("author:@me", "Created"),
    ASSIGNED("assignee:@me", "Assigned"),
    MENTIONED("mentions:@me", "Mentioned"),
    REVIEW_REQUESTED("review-requested:@me", "Review")
}

data class PullRequestItem(
    val repoFullName: String,
    val number: Int,
    val title: String,
    val htmlUrl: String,
    val pullsApiUrl: String,
    val checkStatus: CheckStatus
)

enum class CheckStatus {
    SUCCESS,
    FAILURE,
    PENDING,
    NONE
}

enum class IssueType(val query: String, val displayName: String) {
    ASSIGNED("assignee:@me", "Assigned"),
    CREATED("author:@me", "Created"),
    MENTIONED("mentions:@me", "Mentioned")
}

data class IssueItem(
    val repoFullName: String,
    val number: Int,
    val title: String,
    val htmlUrl: String,
    val state: String
)

data class RepoItem(
    val fullName: String,
    val name: String,
    val ownerLogin: String,
    val description: String?,
    val language: String?,
    val updatedAt: String,
    val isPrivate: Boolean
)

data class WorkflowRunItem(
    val workflowName: String,
    val branch: String,
    val commitMessage: String,
    val status: String,
    val conclusion: String?,
    val htmlUrl: String,
    val event: String,
    val updatedAt: String? = null,
    val runNumber: Int = 0,
    val actor: String? = null,
    val runStartedAt: String? = null
)

data class WorkflowSummary(
    val id: Long,
    val name: String,
    val path: String,
    val state: String,
    val status: String?,
    val conclusion: String?,
    val branch: String?,
    val updatedAt: String?
)