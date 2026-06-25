@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package cn.waijade.nexuswid.ui

import android.content.Context
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.data.github.IssueType
import cn.waijade.nexuswid.data.github.PullRequestItem
import cn.waijade.nexuswid.data.github.PullRequestType
import cn.waijade.nexuswid.ui.settingsScreen.screens.AfdianComplaintPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.AfdianDailyEarningsChartPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.AfdianMonthlyEarningsChartPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.AfdianMonthlyEarningsPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.AfdianProductPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.AfdianRandomCreatorPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.AfdianTopSponsorsPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.AfdianTotalEarningsPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.AfdianUnreadPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.ActionsPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.HeatmapPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.IssuesPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.NotificationsPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.PullRequestsPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.ReviewsRequestedPreviewCard
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.widget.WidgetDataCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.back
import nexuswid.shared.generated.resources.contribution_heatmap
import nexuswid.shared.generated.resources.pull_requests
import nexuswid.shared.generated.resources.reviews_requested
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedTransitionScope.WidgetDetailScreen(
    widgetType: String,
    widgetData: HomeWidgetData,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isDark = isSystemInDarkTheme()
    val loadingBg = if (isDark) Color(0xFF0D1117) else Color.White

    val title = when (widgetType) {
        "contribution_heatmap" -> stringResource(Res.string.contribution_heatmap)
        "reviews_requested" -> stringResource(Res.string.reviews_requested)
        "pull_requests" -> stringResource(Res.string.pull_requests)
        "issues" -> "Issues"
        "actions" -> "Actions"
        "notifications" -> "Notifications"
        "afdian_total_earnings" -> "爱发电总收益"
        "afdian_monthly_earnings" -> "爱发电本月收益"
        "afdian_unread" -> "爱发电未读数"
        "afdian_complaint" -> "爱发电未读投诉"
        "afdian_product" -> "爱发电商品收益"
        "afdian_daily_chart" -> "爱发电每日收益"
        "afdian_monthly_chart" -> "爱发电月度收益"
        "afdian_random_creator" -> "随机推荐创作者"
        "afdian_top_sponsors" -> "赞助月榜"
        else -> "小组件"
    }

    // 从缓存加载数据（首页已缓存过）
    val contributionLevels = remember { mutableStateOf(widgetData.contributionLevels) }
    val prItems = remember { mutableStateOf(widgetData.prItems) }
    val prTypes = remember { mutableStateOf(widgetData.prTypes) }
    val reviewsCount = remember { mutableStateOf(widgetData.reviewsCount) }
    var isHeatmapReady by remember { mutableStateOf(widgetData.isHeatmapLoaded) }
    var isPrReady by remember { mutableStateOf(widgetData.isPrLoaded) }
    var isReviewsReady by remember { mutableStateOf(widgetData.isReviewsLoaded) }

    // 如果首页数据还没加载完（极少情况），从缓存补加载
    LaunchedEffect(widgetType) {
        withContext(Dispatchers.IO) {
            when (widgetType) {
                "contribution_heatmap" -> {
                    if (!widgetData.isHeatmapLoaded) {
                        val prefs = context.getSharedPreferences("widget_data_cache", Context.MODE_PRIVATE)
                        val raw = prefs.getString("cached_contribution_levels", null)
                        contributionLevels.value = if (raw.isNullOrBlank()) emptyMap()
                        else raw.split(",").associate { entry ->
                            val parts = entry.split("=")
                            parts[0] to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
                        }
                        isHeatmapReady = true
                    }
                }
                "pull_requests" -> {
                    if (!widgetData.isPrLoaded) {
                        prTypes.value = GitHubPreferences(context).selectedPullRequestTypes
                        prItems.value = WidgetDataCache.loadPullRequests(context)
                        isPrReady = true
                    }
                }
                "reviews_requested" -> {
                    if (!widgetData.isReviewsLoaded) {
                        reviewsCount.value = WidgetDataCache.loadReviewsCount(context)
                        isReviewsReady = true
                    }
                }
                "issues" -> {
                    if (!widgetData.isIssuesLoaded) {
                        WidgetDataCache.loadIssues(context)
                        isReviewsReady = true
                    }
                }
                "actions", "notifications" -> {
                    // 数据已在首页加载
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        fontSize = 32.sp,
                        lineHeight = 32.sp,
                        modifier = Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState("widget_title_$widgetType"),
                            animatedVisibilityScope = androidx.navigation3.ui.LocalNavAnimatedContentScope.current
                        )
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(Res.string.back)
                        )
                    }
                },
                colors = topBarColors,
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = topBarColors.containerColor,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .sharedBoundsReveal(
                        sharedTransitionScope = this@WidgetDetailScreen,
                        sharedContentState = rememberSharedContentState("widget_preview_$widgetType"),
                        animatedVisibilityScope = androidx.navigation3.ui.LocalNavAnimatedContentScope.current,
                        clipShape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
            ) {
                when (widgetType) {
                    "contribution_heatmap" -> {
                        if (!widgetData.isHeatmapLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            HeatmapPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                weekStartsOnMonday = false,
                                contributionLevels = widgetData.contributionLevels,
                                modifier = Modifier.fillMaxSize(),
                                showPlaceholder = false
                            )
                        }
                    }

                    "reviews_requested" -> {
                        if (!widgetData.isReviewsLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            ReviewsRequestedPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                count = widgetData.reviewsCount,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "pull_requests" -> {
                        if (!widgetData.isPrLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            PullRequestsPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                prItems = widgetData.prItems,
                                prTypes = widgetData.prTypes,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "issues" -> {
                        if (!widgetData.isIssuesLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            IssuesPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                issueItems = widgetData.issueItems,
                                issueTypes = widgetData.issueTypes,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "actions" -> {
                        if (!widgetData.isActionsLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            ActionsPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                actionRuns = widgetData.actionRuns,
                                repo = widgetData.actionsRepo,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "notifications" -> {
                        if (!widgetData.isNotificationsLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            NotificationsPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                notificationItems = widgetData.notificationItems,
                                unreadCount = widgetData.notificationUnreadCount,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "afdian_total_earnings" -> {
                        if (!widgetData.isAfdianTotalLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            AfdianTotalEarningsPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                totalEarnings = widgetData.afdianTotalEarnings,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "afdian_monthly_earnings" -> {
                        if (!widgetData.isAfdianMonthlyLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            AfdianMonthlyEarningsPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                monthlyEarnings = widgetData.afdianMonthlyEarnings,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "afdian_unread" -> {
                        if (!widgetData.isAfdianUnreadLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            AfdianUnreadPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                unreadCount = widgetData.afdianUnreadCount,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "afdian_complaint" -> {
                        if (!widgetData.isAfdianComplaintLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            AfdianComplaintPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                complaintCount = widgetData.afdianComplaintCount,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "afdian_product" -> {
                        if (!widgetData.isAfdianProductLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            AfdianProductPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                productData = widgetData.afdianProductData,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "afdian_daily_chart" -> {
                        if (!widgetData.isAfdianDailyLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            AfdianDailyEarningsChartPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                dailyStats = widgetData.afdianDailyStats,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "afdian_monthly_chart" -> {
                        if (!widgetData.isAfdianMonthlyIncomeLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            AfdianMonthlyEarningsChartPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                monthlyIncomes = widgetData.afdianMonthlyIncomes,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "afdian_random_creator" -> {
                        if (!widgetData.isAfdianRandomCreatorLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            AfdianRandomCreatorPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                randomCreator = widgetData.afdianRandomCreator,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    "afdian_top_sponsors" -> {
                        if (!widgetData.isAfdianTopSponsorsLoaded) {
                            LoadingPlaceholder(loadingBg)
                        } else {
                            AfdianTopSponsorsPreviewCard(
                                colorMode = HeatmapColorMode.SYSTEM,
                                topSponsors = widgetData.afdianTopSponsors,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (widgetType) {
                    "contribution_heatmap" -> "显示您的 GitHub 贡献热力图"
                    "reviews_requested" -> "显示请求的代码审查数量"
                    "pull_requests" -> "显示拉取请求列表和状态"
                    "issues" -> "显示分配的 Issue 列表"
                    "actions" -> "显示 GitHub Actions 工作流运行状态"
                    "notifications" -> "显示 GitHub 通知列表"
                    "afdian_total_earnings" -> "显示爱发电累计总收益"
                    "afdian_monthly_earnings" -> "显示爱发电本月收益"
                    "afdian_unread" -> "显示爱发电未读消息数量"
                    "afdian_complaint" -> "显示爱发电未读投诉数量"
                    "afdian_product" -> "显示爱发电商品收益详情"
                    "afdian_daily_chart" -> "显示爱发电每日收益折线图"
                    "afdian_monthly_chart" -> "显示爱发电月度收益折线图"
                    "afdian_random_creator" -> "随机推荐一位爱发电创作者"
                    "afdian_top_sponsors" -> "显示爱发电赞助月榜"
                    else -> ""
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingPlaceholder(bgColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .height(24.dp)
                .width(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
