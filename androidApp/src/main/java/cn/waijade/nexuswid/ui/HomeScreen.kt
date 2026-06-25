@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package cn.waijade.nexuswid.ui

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.waijade.nexuswid.data.HeatmapColorMode
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
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.actions
import nexuswid.shared.generated.resources.contribution_heatmap
import nexuswid.shared.generated.resources.issues
import nexuswid.shared.generated.resources.notifications
import nexuswid.shared.generated.resources.pull_requests
import nexuswid.shared.generated.resources.reviews_requested
import nexuswid.shared.generated.resources.widget_afdian_complaint
import nexuswid.shared.generated.resources.widget_afdian_daily_chart
import nexuswid.shared.generated.resources.widget_afdian_monthly
import nexuswid.shared.generated.resources.widget_afdian_monthly_chart
import nexuswid.shared.generated.resources.widget_afdian_product
import nexuswid.shared.generated.resources.widget_afdian_random_creator
import nexuswid.shared.generated.resources.widget_afdian_top_sponsors
import nexuswid.shared.generated.resources.widget_afdian_total
import nexuswid.shared.generated.resources.widget_afdian_unread
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    widgetData: HomeWidgetData,
    contentPadding: PaddingValues,
    onWidgetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isDark = isSystemInDarkTheme()
    val loadingBg = if (isDark) Color(0xFF0D1117) else Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NexusWid",
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        fontSize = 32.sp,
                        lineHeight = 32.sp
                    )
                },
                subtitle = {},
                titleHorizontalAlignment = Alignment.CenterHorizontally,
                colors = topBarColors,
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = topBarColors.containerColor,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(insets)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // === GitHub 小组件 ===

            // 贡献热力图 - 宽4
            WidgetPreviewItem(
                id = "contribution_heatmap",
                title = stringResource(Res.string.contribution_heatmap),
                widthCells = 4,
                heightDp = 180.dp,
                onClick = { onWidgetClick("contribution_heatmap") }
            ) {
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

            // 拉取请求 - 宽4
            WidgetPreviewItem(
                id = "pull_requests",
                title = stringResource(Res.string.pull_requests),
                widthCells = 4,
                heightDp = 180.dp,
                onClick = { onWidgetClick("pull_requests") }
            ) {
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

            // 审查请求 - 宽2
            WidgetPreviewItem(
                id = "reviews_requested",
                title = stringResource(Res.string.reviews_requested),
                widthCells = 2,
                heightDp = 180.dp,
                onClick = { onWidgetClick("reviews_requested") }
            ) {
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

            // Issues - 宽2
            WidgetPreviewItem(
                id = "issues",
                title = stringResource(Res.string.issues),
                widthCells = 2,
                heightDp = 180.dp,
                onClick = { onWidgetClick("issues") }
            ) {
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

            // Actions - 宽4
            WidgetPreviewItem(
                id = "actions",
                title = stringResource(Res.string.actions),
                widthCells = 4,
                heightDp = 180.dp,
                onClick = { onWidgetClick("actions") }
            ) {
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

            // Notifications - 宽4
            WidgetPreviewItem(
                id = "notifications",
                title = stringResource(Res.string.notifications),
                widthCells = 4,
                heightDp = 180.dp,
                onClick = { onWidgetClick("notifications") }
            ) {
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

            // === 爱发电小组件 ===

            // 爱发电总收益 - 宽2高2
            WidgetPreviewItem(
                id = "afdian_total_earnings",
                title = stringResource(Res.string.widget_afdian_total),
                widthCells = 2,
                heightDp = 160.dp,
                onClick = { onWidgetClick("afdian_total_earnings") }
            ) {
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

            // 爱发电本月收益 - 宽2高2
            WidgetPreviewItem(
                id = "afdian_monthly_earnings",
                title = stringResource(Res.string.widget_afdian_monthly),
                widthCells = 2,
                heightDp = 160.dp,
                onClick = { onWidgetClick("afdian_monthly_earnings") }
            ) {
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

            // 爱发电未读数 - 宽2高2
            WidgetPreviewItem(
                id = "afdian_unread",
                title = stringResource(Res.string.widget_afdian_unread),
                widthCells = 2,
                heightDp = 160.dp,
                onClick = { onWidgetClick("afdian_unread") }
            ) {
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

            // 爱发电未读投诉 - 宽2高2
            WidgetPreviewItem(
                id = "afdian_complaint",
                title = stringResource(Res.string.widget_afdian_complaint),
                widthCells = 2,
                heightDp = 160.dp,
                onClick = { onWidgetClick("afdian_complaint") }
            ) {
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

            // 爱发电商品收益 - 宽4
            WidgetPreviewItem(
                id = "afdian_product",
                title = stringResource(Res.string.widget_afdian_product),
                widthCells = 4,
                heightDp = 180.dp,
                onClick = { onWidgetClick("afdian_product") }
            ) {
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

            // 爱发电每日收益图表 - 宽4
            WidgetPreviewItem(
                id = "afdian_daily_chart",
                title = stringResource(Res.string.widget_afdian_daily_chart),
                widthCells = 4,
                heightDp = 200.dp,
                onClick = { onWidgetClick("afdian_daily_chart") }
            ) {
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

            // 爱发电月度收益图表 - 宽4
            WidgetPreviewItem(
                id = "afdian_monthly_chart",
                title = stringResource(Res.string.widget_afdian_monthly_chart),
                widthCells = 4,
                heightDp = 200.dp,
                onClick = { onWidgetClick("afdian_monthly_chart") }
            ) {
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

            // 随机推荐创作者 - 宽2
            WidgetPreviewItem(
                id = "afdian_random_creator",
                title = stringResource(Res.string.widget_afdian_random_creator),
                widthCells = 2,
                heightDp = 180.dp,
                onClick = { onWidgetClick("afdian_random_creator") }
            ) {
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

            // 赞助月榜 - 宽2
            WidgetPreviewItem(
                id = "afdian_top_sponsors",
                title = stringResource(Res.string.widget_afdian_top_sponsors),
                widthCells = 2,
                heightDp = 180.dp,
                onClick = { onWidgetClick("afdian_top_sponsors") }
            ) {
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

@Composable
private fun SharedTransitionScope.WidgetPreviewItem(
    id: String,
    title: String,
    widthCells: Int,
    heightDp: Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val widthFraction = widthCells / 4f

    Column(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .padding(horizontal = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp)
                .sharedBoundsReveal(
                    sharedTransitionScope = this@WidgetPreviewItem,
                    sharedContentState = rememberSharedContentState("widget_preview_$id"),
                    animatedVisibilityScope = androidx.navigation3.ui.LocalNavAnimatedContentScope.current,
                    clipShape = RoundedCornerShape(16.dp)
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
                onClick = onClick
            ) {
                content()
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}
