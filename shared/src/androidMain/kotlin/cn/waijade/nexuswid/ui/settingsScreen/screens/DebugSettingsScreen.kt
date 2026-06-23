@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package cn.waijade.nexuswid.ui.settingsScreen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.ui.mergePaddingValues
import cn.waijade.nexuswid.ui.theme.CustomColors.detailPaneTopBarColors
import cn.waijade.nexuswid.ui.theme.CustomColors.listItemColors
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.PANE_MAX_WIDTH
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.segmentedListItemShapes
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.back
import nexuswid.shared.generated.resources.settings
import org.jetbrains.compose.resources.stringResource
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent

@Composable
fun DebugSettingsScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val githubPreferences = remember { GitHubPreferences(context) }
    var debugCountValue by remember { mutableIntStateOf(githubPreferences.debugCountValue) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val barColors = if (widthExpanded) detailPaneTopBarColors
    else topBarColors

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(barColors.containerColor)
    ) {
        Scaffold(
            topBar = {
                LargeFlexibleTopAppBar(
                    title = {
                        Text(
                            "Debug",
                            fontFamily = LocalAppFonts.current.topBarTitle
                        )
                    },
                    subtitle = {
                        Text(stringResource(Res.string.settings))
                    },
                    navigationIcon = {
                        if (!widthExpanded)
                            FilledTonalIconButton(
                                onClick = onBack,
                                shapes = IconButtonDefaults.shapes(),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = listItemColors.containerColor
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    stringResource(Res.string.back)
                                )
                            }
                    },
                    colors = barColors,
                    scrollBehavior = scrollBehavior
                )
            },
            containerColor = barColors.containerColor,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            val insets = mergePaddingValues(innerPadding, contentPadding)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = insets,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    SegmentedListItem(
                        onClick = {},
                        content = { Text("PR小组件数字值") },
                        supportingContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = "当前值: $debugCountValue",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = debugCountValue.toFloat(),
                                    onValueChange = { newValue ->
                                        debugCountValue = newValue.toInt()
                                        githubPreferences.debugCountValue = newValue.toInt()
                                        // 触发小组件更新
                                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                                            val componentName = ComponentName(context.packageName, "cn.waijade.nexuswid.widget.ReviewsRequestedWidgetReceiver")
                                            val appWidgetManager = AppWidgetManager.getInstance(context)
                                            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                                        }
                                        context.sendBroadcast(intent)
                                    },
                                    valueRange = 0f..100000f,
                                    steps = 100
                                )
                            }
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(0, 0)
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    var useTestData by remember { mutableStateOf(githubPreferences.debugUseTestData) }
                    SegmentedListItem(
                        onClick = {
                            useTestData = !useTestData
                            githubPreferences.debugUseTestData = useTestData
                            // 触发小组件更新
                            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                                val componentName = ComponentName(context.packageName, "cn.waijade.nexuswid.widget.PullRequestsWidgetReceiver")
                                val appWidgetManager = AppWidgetManager.getInstance(context)
                                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                            }
                            context.sendBroadcast(intent)
                        },
                        content = { Text("PR列表使用测试数据") },
                        supportingContent = {
                            Text(
                                text = "开启后PR列表小组件将显示预置的测试数据",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = useTestData,
                                onCheckedChange = null
                            )
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(0, 0)
                    )
                }
            }
        }
    }
}
