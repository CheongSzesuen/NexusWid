@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package cn.waijade.nexuswid.ui

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.ui.settingsScreen.screens.HeatmapPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.PullRequestsPreviewCard
import cn.waijade.nexuswid.ui.settingsScreen.screens.ReviewsRequestedPreviewCard
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.widget.HeatmapWidgetDataStore
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
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val title = when (widgetType) {
        "contribution_heatmap" -> stringResource(Res.string.contribution_heatmap)
        "reviews_requested" -> stringResource(Res.string.reviews_requested)
        "pull_requests" -> stringResource(Res.string.pull_requests)
        else -> "小组件"
    }

    val contributionLevels = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    LaunchedEffect(widgetType) {
        if (widgetType == "contribution_heatmap") {
            withContext(Dispatchers.IO) {
                val dataStore = HeatmapWidgetDataStore(context)
                contributionLevels.value = dataStore.getContributionLevels()
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
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                        HeatmapPreviewCard(
                            colorMode = HeatmapColorMode.SYSTEM,
                            weekStartsOnMonday = false,
                            contributionLevels = contributionLevels.value,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    "reviews_requested" -> {
                        ReviewsRequestedPreviewCard(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    "pull_requests" -> {
                        PullRequestsPreviewCard(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (widgetType) {
                    "contribution_heatmap" -> "显示您的 GitHub 贡献热力图"
                    "reviews_requested" -> "显示请求的代码审查数量"
                    "pull_requests" -> "显示拉取请求列表和状态"
                    else -> ""
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}