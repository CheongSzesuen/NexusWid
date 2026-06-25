package cn.waijade.nexuswid.ui

import cn.waijade.nexuswid.data.afdian.AfdianDailyStat
import cn.waijade.nexuswid.data.afdian.AfdianMonthlyIncome
import cn.waijade.nexuswid.data.afdian.AfdianProductSummary
import cn.waijade.nexuswid.data.afdian.AfdianRandomCreator
import cn.waijade.nexuswid.data.afdian.AfdianTopSponsor
import cn.waijade.nexuswid.data.github.IssueItem
import cn.waijade.nexuswid.data.github.IssueType
import cn.waijade.nexuswid.data.github.NotificationItem
import cn.waijade.nexuswid.data.github.PullRequestItem
import cn.waijade.nexuswid.data.github.PullRequestType
import cn.waijade.nexuswid.data.github.WorkflowRunItem

data class HomeWidgetData(
    // Contribution Heatmap
    val contributionLevels: Map<String, Int> = emptyMap(),
    val isHeatmapLoaded: Boolean = false,

    // Pull Requests
    val prItems: List<PullRequestItem> = emptyList(),
    val prTypes: Set<PullRequestType> = emptySet(),
    val isPrLoaded: Boolean = false,

    // Reviews Requested
    val reviewsCount: Int = -1,
    val isReviewsLoaded: Boolean = false,

    // Issues
    val issueItems: List<IssueItem> = emptyList(),
    val issueTypes: Set<IssueType> = emptySet(),
    val isIssuesLoaded: Boolean = false,

    // Actions
    val actionRuns: List<WorkflowRunItem> = emptyList(),
    val actionsRepo: String = "",
    val isActionsLoaded: Boolean = false,

    // Notifications
    val notificationItems: List<NotificationItem> = emptyList(),
    val notificationUnreadCount: Int = 0,
    val isNotificationsLoaded: Boolean = false,

    // Afdian Total Earnings
    val afdianTotalEarnings: Double? = null,
    val isAfdianTotalLoaded: Boolean = false,

    // Afdian Monthly Earnings
    val afdianMonthlyEarnings: Double? = null,
    val isAfdianMonthlyLoaded: Boolean = false,

    // Afdian Unread
    val afdianUnreadCount: Int? = null,
    val isAfdianUnreadLoaded: Boolean = false,

    // Afdian Product
    val afdianProductData: AfdianProductSummary? = null,
    val isAfdianProductLoaded: Boolean = false,

    // Afdian Daily Earnings Chart
    val afdianDailyStats: List<AfdianDailyStat> = emptyList(),
    val isAfdianDailyLoaded: Boolean = false,

    // Afdian Monthly Earnings Chart
    val afdianMonthlyIncomes: List<AfdianMonthlyIncome> = emptyList(),
    val isAfdianMonthlyIncomeLoaded: Boolean = false,

    // Afdian Complaint
    val afdianComplaintCount: Int? = null,
    val isAfdianComplaintLoaded: Boolean = false,

    // Afdian Random Creator
    val afdianRandomCreator: AfdianRandomCreator? = null,
    val isAfdianRandomCreatorLoaded: Boolean = false,

    // Afdian Top Sponsors
    val afdianTopSponsors: List<AfdianTopSponsor> = emptyList(),
    val isAfdianTopSponsorsLoaded: Boolean = false,

    // Configuration states
    val githubConfigured: Boolean = false,
    val afdianConfigured: Boolean = false,
)
