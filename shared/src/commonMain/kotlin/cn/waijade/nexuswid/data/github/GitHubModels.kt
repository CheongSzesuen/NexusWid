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