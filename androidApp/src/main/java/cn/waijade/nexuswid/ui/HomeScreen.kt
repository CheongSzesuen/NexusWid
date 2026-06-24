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
import cn.waijade.nexuswid.ui.settingsScreen.screens.HeatmapPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.PullRequestsPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.ReviewsRequestedPreviewCard
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.contribution_heatmap
import nexuswid.shared.generated.resources.pull_requests
import nexuswid.shared.generated.resources.reviews_requested
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(insets)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // 贡献热力图 - 宽4高2
            WidgetPreviewItem(
                id = "contribution_heatmap",
                title = stringResource(Res.string.contribution_heatmap),
                widthCells = 4,
                heightDp = 160.dp,
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

            // 拉取请求 - 宽4高2
            WidgetPreviewItem(
                id = "pull_requests",
                title = stringResource(Res.string.pull_requests),
                widthCells = 4,
                heightDp = 160.dp,
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

            // 审查请求 - 宽2高2
            WidgetPreviewItem(
                id = "reviews_requested",
                title = stringResource(Res.string.reviews_requested),
                widthCells = 2,
                heightDp = 160.dp,
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
        modifier = Modifier.fillMaxWidth(widthFraction)
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
