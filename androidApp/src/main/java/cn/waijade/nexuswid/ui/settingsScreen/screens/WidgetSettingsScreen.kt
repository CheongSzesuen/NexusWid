@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package cn.waijade.nexuswid.ui.settingsScreen.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.HeatmapTheme
import cn.waijade.nexuswid.data.github.PullRequestType
import cn.waijade.nexuswid.ui.mergePaddingValues
import cn.waijade.nexuswid.ui.theme.CustomColors.detailPaneTopBarColors
import cn.waijade.nexuswid.ui.theme.CustomColors.listItemColors
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.PANE_MAX_WIDTH
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.segmentedListItemShapes
import cn.waijade.nexuswid.widget.ContributionHeatmapWidgetProvider
import cn.waijade.nexuswid.widget.HeatmapGridCalculator
import cn.waijade.nexuswid.widget.HeatmapWidgetDataStore
import cn.waijade.nexuswid.widget.PullRequestsWidgetReceiver
import cn.waijade.nexuswid.widget.ReviewsRequestedWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.add_to_home_screen
import nexuswid.shared.generated.resources.back
import nexuswid.shared.generated.resources.contribution_widget_preview_hint
import nexuswid.shared.generated.resources.dark
import nexuswid.shared.generated.resources.dark_mode
import nexuswid.shared.generated.resources.heatmap_color_mode
import nexuswid.shared.generated.resources.ic_brightness_auto
import nexuswid.shared.generated.resources.light
import nexuswid.shared.generated.resources.light_mode
import nexuswid.shared.generated.resources.palette
import nexuswid.shared.generated.resources.settings
import nexuswid.shared.generated.resources.system_default
import nexuswid.shared.generated.resources.week_start_day
import nexuswid.shared.generated.resources.week_start_monday
import nexuswid.shared.generated.resources.week_start_sunday
import nexuswid.shared.generated.resources.widget
import nexuswid.shared.generated.resources.widget_contribution
import nexuswid.shared.generated.resources.widget_pull_requests
import nexuswid.shared.generated.resources.widget_reviews_requested
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

private const val PREVIEW_CONTENT_PADDING_DP = 6f
private const val PREVIEW_CELL_SIZE_SCALE = 1f
private const val PREVIEW_CORNER_RADIUS_DP = 12f

@Composable
fun WidgetSettingsScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    heatmapColorMode: HeatmapColorMode,
    onHeatmapColorModeChange: (HeatmapColorMode) -> Unit,
    weekStartsOnMonday: Boolean,
    onWeekStartsOnMondayChange: (Boolean) -> Unit,
    selectedPullRequestTypes: Set<PullRequestType>,
    onPullRequestTypesChange: (Set<PullRequestType>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val barColors = if (widthExpanded) detailPaneTopBarColors
    else topBarColors

    val contributionLevels = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dataStore = HeatmapWidgetDataStore(context)
            contributionLevels.value = dataStore.getContributionLevels()
        }
    }

    var previewFrameWidthPx by rememberSaveable { mutableStateOf(0f) }

    val colorModeOptions = remember {
        listOf(
            Triple(HeatmapColorMode.SYSTEM, Res.drawable.ic_brightness_auto, Res.string.system_default),
            Triple(HeatmapColorMode.LIGHT, Res.drawable.light_mode, Res.string.light),
            Triple(HeatmapColorMode.DARK, Res.drawable.dark_mode, Res.string.dark)
        )
    }
    val currentColorModeIcon = remember(heatmapColorMode) {
        colorModeOptions.first { it.first == heatmapColorMode }.second
    }

    val weekStartOptions = remember {
        listOf(
            false to Res.string.week_start_sunday,
            true to Res.string.week_start_monday
        )
    }

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
                            stringResource(Res.string.widget),
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
                item { Spacer(Modifier.height(14.dp)) }

                item {
                    Text(
                        text = stringResource(Res.string.widget_contribution),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    SegmentedListItem(
                        onClick = {},
                        content = { Text(stringResource(Res.string.widget_contribution)) },
                        supportingContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                BoxWithConstraints(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val maxWidthPx = with(density) { maxWidth.toPx() }
                                    val minWidthPx = with(density) { 120.dp.toPx() }
                                    val effectiveMinWidthPx = minWidthPx.coerceAtMost(maxWidthPx)
                                    val coercedWidthPx =
                                        previewFrameWidthPx.coerceIn(effectiveMinWidthPx, maxWidthPx)
                                    val frameWidthDp = with(density) { coercedWidthPx.toDp() }
                                    LaunchedEffect(maxWidthPx) {
                                        if (previewFrameWidthPx <= 0f) {
                                            previewFrameWidthPx = maxWidthPx
                                        } else {
                                            previewFrameWidthPx =
                                                previewFrameWidthPx.coerceIn(
                                                    effectiveMinWidthPx,
                                                    maxWidthPx
                                                )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(frameWidthDp)
                                                .height(160.dp)
                                                .pointerInput(maxWidthPx) {
                                                    detectHorizontalDragGestures { _, dragAmount ->
                                                        previewFrameWidthPx =
                                                            (previewFrameWidthPx + dragAmount)
                                                                .coerceIn(
                                                                    effectiveMinWidthPx,
                                                                    maxWidthPx
                                                                )
                                                    }
                                                }
                                        ) {
                                            HeatmapPreviewCard(
                                                colorMode = heatmapColorMode,
                                                weekStartsOnMonday = weekStartsOnMonday,
                                                contributionLevels = contributionLevels.value,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            val message =
                                                when (requestPinContributionHeatmapWidget(context)) {
                                                    PinWidgetRequestResult.REQUESTED -> "请在系统弹窗中确认添加小组件"
                                                    PinWidgetRequestResult.NOT_SUPPORTED -> "当前桌面不支持一键添加小组件"
                                                    PinWidgetRequestResult.UNSUPPORTED_ANDROID -> "当前安卓版本不支持一键添加"
                                                    PinWidgetRequestResult.FAILED -> "添加请求发送失败，请手动添加"
                                                }
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Add,
                                            contentDescription = null,
                                            modifier = Modifier.height(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(Res.string.add_to_home_screen))
                                    }
                                }
                            }
                        },
                        leadingContent = {
                            Icon(painterResource(Res.drawable.palette), null)
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(0, 3)
                    )
                }

                item {
                    SegmentedListItem(
                        onClick = {},
                        leadingContent = {
                            AnimatedContent(currentColorModeIcon) {
                                Icon(
                                    painter = painterResource(it),
                                    contentDescription = null
                                )
                            }
                        },
                        content = { Text(stringResource(Res.string.heatmap_color_mode)) },
                        supportingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                colorModeOptions.fastForEachIndexed { index, entry ->
                                    val isSelected = heatmapColorMode == entry.first
                                    ToggleButton(
                                        checked = isSelected,
                                        onCheckedChange = { onHeatmapColorModeChange(entry.first) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics { role = Role.RadioButton },
                                        shapes = when (index) {
                                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                            colorModeOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        }
                                    ) {
                                        Text(
                                            stringResource(entry.third),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(1, 3)
                    )
                }

                item {
                    SegmentedListItem(
                        onClick = {},
                        leadingContent = {
                            Icon(Icons.Outlined.DateRange, null)
                        },
                        content = { Text(stringResource(Res.string.week_start_day)) },
                        supportingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                weekStartOptions.fastForEachIndexed { index, entry ->
                                    val isSelected = weekStartsOnMonday == entry.first
                                    ToggleButton(
                                        checked = isSelected,
                                        onCheckedChange = { onWeekStartsOnMondayChange(entry.first) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics { role = Role.RadioButton },
                                        shapes = when (index) {
                                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                            weekStartOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        }
                                    ) {
                                        Text(
                                            stringResource(entry.second),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(2, 3)
                    )
                }

                item {
                    Text(
                        text = stringResource(Res.string.contribution_widget_preview_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    Text(
                        text = stringResource(Res.string.widget_reviews_requested),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    SegmentedListItem(
                        onClick = {},
                        content = { Text(stringResource(Res.string.widget_reviews_requested)) },
                        supportingContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .height(160.dp)
                                    ) {
                                        ReviewsRequestedPreviewCard(
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "数字缩略: 1k = 1000, 1M = 1000000",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            val message =
                                                when (requestPinReviewsRequestedWidget(context)) {
                                                    PinWidgetRequestResult.REQUESTED -> "请在系统弹窗中确认添加小组件"
                                                    PinWidgetRequestResult.NOT_SUPPORTED -> "当前桌面不支持一键添加小组件"
                                                    PinWidgetRequestResult.UNSUPPORTED_ANDROID -> "当前安卓版本不支持一键添加"
                                                    PinWidgetRequestResult.FAILED -> "添加请求发送失败，请手动添加"
                                                }
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Add,
                                            contentDescription = null,
                                            modifier = Modifier.height(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(Res.string.add_to_home_screen))
                                    }
                                }
                            }
                        },
                        leadingContent = {
                            Icon(painterResource(Res.drawable.palette), null)
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(0, 2)
                    )
                }

                item {
                    SegmentedListItem(
                        onClick = {},
                        content = { Text("包含的 PR 类型") },
                        supportingContent = {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PullRequestType.entries.forEach { type ->
                                    val isSelected = selectedPullRequestTypes.contains(type)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            val newTypes = if (isSelected) {
                                                selectedPullRequestTypes - type
                                            } else {
                                                selectedPullRequestTypes + type
                                            }
                                            onPullRequestTypesChange(newTypes)
                                        },
                                        label = { Text(type.displayName) }
                                    )
                                }
                            }
                        },
                        leadingContent = {
                            Icon(painterResource(Res.drawable.palette), null)
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(1, 2)
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    Text(
                        text = stringResource(Res.string.widget_pull_requests),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    SegmentedListItem(
                        onClick = {},
                        content = { Text(stringResource(Res.string.widget_pull_requests)) },
                        supportingContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    ) {
                                        PullRequestsPreviewCard(
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            val message =
                                                when (requestPinPullRequestsWidget(context)) {
                                                    PinWidgetRequestResult.REQUESTED -> "请在系统弹窗中确认添加小组件"
                                                    PinWidgetRequestResult.NOT_SUPPORTED -> "当前桌面不支持一键添加小组件"
                                                    PinWidgetRequestResult.UNSUPPORTED_ANDROID -> "当前安卓版本不支持一键添加"
                                                    PinWidgetRequestResult.FAILED -> "添加请求发送失败，请手动添加"
                                                }
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Add,
                                            contentDescription = null,
                                            modifier = Modifier.height(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(Res.string.add_to_home_screen))
                                    }
                                }
                            }
                        },
                        leadingContent = {
                            Icon(painterResource(Res.drawable.palette), null)
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(0, 2)
                    )
                }

                item {
                    SegmentedListItem(
                        onClick = {},
                        content = { Text("包含的 PR 类型") },
                        supportingContent = {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PullRequestType.entries.forEach { type ->
                                    val isSelected = selectedPullRequestTypes.contains(type)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            val newTypes = if (isSelected) {
                                                selectedPullRequestTypes - type
                                            } else {
                                                selectedPullRequestTypes + type
                                            }
                                            onPullRequestTypesChange(newTypes)
                                        },
                                        label = { Text(type.displayName) }
                                    )
                                }
                            }
                        },
                        leadingContent = {
                            Icon(painterResource(Res.drawable.palette), null)
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(1, 2)
                    )
                }
            }
        }
    }
}

private enum class PinWidgetRequestResult {
    REQUESTED,
    NOT_SUPPORTED,
    UNSUPPORTED_ANDROID,
    FAILED
}

private fun requestPinContributionHeatmapWidget(context: Context): PinWidgetRequestResult {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return PinWidgetRequestResult.UNSUPPORTED_ANDROID
    }
    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        ?: return PinWidgetRequestResult.FAILED
    if (!appWidgetManager.isRequestPinAppWidgetSupported) {
        return PinWidgetRequestResult.NOT_SUPPORTED
    }
    val provider = ComponentName(context, ContributionHeatmapWidgetProvider::class.java)
    return if (appWidgetManager.requestPinAppWidget(provider, null, null)) {
        PinWidgetRequestResult.REQUESTED
    } else {
        PinWidgetRequestResult.FAILED
    }
}

private fun requestPinReviewsRequestedWidget(context: Context): PinWidgetRequestResult {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return PinWidgetRequestResult.UNSUPPORTED_ANDROID
    }
    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        ?: return PinWidgetRequestResult.FAILED
    if (!appWidgetManager.isRequestPinAppWidgetSupported) {
        return PinWidgetRequestResult.NOT_SUPPORTED
    }
    val provider = ComponentName(context, ReviewsRequestedWidgetProvider::class.java)
    return if (appWidgetManager.requestPinAppWidget(provider, null, null)) {
        PinWidgetRequestResult.REQUESTED
    } else {
        PinWidgetRequestResult.FAILED
    }
}

private fun requestPinPullRequestsWidget(context: Context): PinWidgetRequestResult {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return PinWidgetRequestResult.UNSUPPORTED_ANDROID
    }
    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        ?: return PinWidgetRequestResult.FAILED
    if (!appWidgetManager.isRequestPinAppWidgetSupported) {
        return PinWidgetRequestResult.NOT_SUPPORTED
    }
    val provider = ComponentName(context, PullRequestsWidgetReceiver::class.java)
    return if (appWidgetManager.requestPinAppWidget(provider, null, null)) {
        PinWidgetRequestResult.REQUESTED
    } else {
        PinWidgetRequestResult.FAILED
    }
}

@Composable
private fun HeatmapPreviewCard(
    colorMode: HeatmapColorMode,
    weekStartsOnMonday: Boolean,
    contributionLevels: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> systemDark
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }
    val theme = remember { HeatmapTheme.resolveCurrentTheme() }
    val heatmapPalette = remember(isDark, theme) { theme.palette(isDark) }
    val palette = remember(isDark, theme) {
        listOf(heatmapPalette.level0, heatmapPalette.level1, heatmapPalette.level2, heatmapPalette.level3, heatmapPalette.level4)
    }
    val containerBg = remember(isDark, theme) { heatmapPalette.containerBg }
    val borderColor = remember(isDark, theme) { heatmapPalette.border }
    val previewSeed = rememberSaveable { Random.nextInt() }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(PREVIEW_CORNER_RADIUS_DP.dp),
        color = borderColor
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val rows = HeatmapGridCalculator.ROWS
            val contentHorizontalInset = (1f + PREVIEW_CONTENT_PADDING_DP) * 2f
            val contentVerticalInset = (1f + PREVIEW_CONTENT_PADDING_DP) * 2f
            val contentWidthDp = (maxWidth.value - contentHorizontalInset).coerceAtLeast(1f)
            val contentHeightDp = (maxHeight.value - contentVerticalInset).coerceAtLeast(1f)
            val columns = remember(contentWidthDp, contentHeightDp) {
                HeatmapGridCalculator.calculateColumns(
                    widthDp = contentWidthDp,
                    heightDp = contentHeightDp
                )
            }
            val cellSize: Dp = remember(contentWidthDp, contentHeightDp, columns) {
                HeatmapGridCalculator.calculateCellSize(
                    widthDp = contentWidthDp,
                    heightDp = contentHeightDp,
                    columns = columns
                ).times(PREVIEW_CELL_SIZE_SCALE).dp
            }
            val gap = HeatmapGridCalculator.GAP_DP.dp
            val sample = remember(columns, weekStartsOnMonday, contributionLevels) {
                if (contributionLevels.isEmpty()) {
                    val random = Random(previewSeed)
                    List(columns * rows) { random.nextInt(5) }
                } else {
                    val dataStore = HeatmapWidgetDataStore(context)
                    dataStore.buildGridFromLevels(
                        levelsByDate = contributionLevels,
                        columns = columns,
                        rows = rows,
                        weekStartsOnMonday = weekStartsOnMonday
                    )
                }
            }
            val gridWidth = cellSize * columns + gap * (columns - 1)
            val gridHeight = cellSize * rows + gap * (rows - 1)

            Surface(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize(),
                shape = RoundedCornerShape((PREVIEW_CORNER_RADIUS_DP - 1).dp),
                color = containerBg
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = PREVIEW_CONTENT_PADDING_DP.dp,
                            vertical = PREVIEW_CONTENT_PADDING_DP.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .width(gridWidth)
                            .height(gridHeight),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (week in 0 until columns) {
                            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                                for (day in 0 until rows) {
                                    val index = week * rows + day
                                    val level = sample.getOrNull(index)?.coerceIn(0, 4) ?: 0
                                    val color: Color = palette[level]
                                    Surface(
                                        color = color,
                                        shape = RoundedCornerShape(cellSize * 0.24f),
                                        modifier = Modifier
                                            .width(cellSize)
                                            .height(cellSize)
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class PRPreviewStatus { SUCCESS, FAILURE }

@Composable
private fun PRPreviewRow(
    repo: String,
    prNumber: Int,
    title: String,
    status: PRPreviewStatus,
    grayText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$repo #$prNumber",
                color = grayText,
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(
                    when (status) {
                        PRPreviewStatus.SUCCESS -> cn.waijade.nexuswid.R.drawable.ic_check_circle_green
                        PRPreviewStatus.FAILURE -> cn.waijade.nexuswid.R.drawable.ic_x_circle_red
                    }
                ),
                contentDescription = null,
                tint = when (status) {
                    PRPreviewStatus.SUCCESS -> Color(0xFF1F883D)
                    PRPreviewStatus.FAILURE -> Color(0xFFCF222E)
                },
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(1.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PullRequestsPreviewCard(
    modifier: Modifier = Modifier
) {
    val githubGreen = Color(0xFF1F883D)
    val grayText = Color(0xFF8B949E)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0D1117)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(cn.waijade.nexuswid.R.drawable.ic_git_pull_request_green),
                        contentDescription = null,
                        tint = githubGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "2 reviews requested",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(cn.waijade.nexuswid.R.drawable.ic_mark_github),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                PRPreviewRow(
                    repo = "github/docs",
                    prNumber = 4287,
                    title = "Fix broken links in API reference",
                    status = PRPreviewStatus.SUCCESS,
                    grayText = grayText
                )

                PRPreviewRow(
                    repo = "kubernetes/kubernetes",
                    prNumber = 131204,
                    title = "Update node autoscaler config",
                    status = PRPreviewStatus.FAILURE,
                    grayText = grayText
                )
            }
        }
    }
}

@Composable
private fun ReviewsRequestedPreviewCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Black
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            val contentHeight = maxHeight
            val contentWidth = maxWidth
            val iconSize = (contentHeight * 0.15f).coerceAtMost(contentWidth * 0.3f)
            val labelSize = (contentHeight * 0.12f).coerceAtMost(18.dp)
            val countTextSize = (contentHeight * 0.45f).coerceAtMost(contentWidth * 0.5f)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Icon(
                    painter = painterResource(cn.waijade.nexuswid.R.drawable.ic_git_pull_request),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .width(iconSize)
                        .height(iconSize)
                )
                Text(
                    text = "3",
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = countTextSize.value.sp
                    )
                )
                Column {
                    Text(
                        text = "Reviews",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = labelSize.value.sp
                        )
                    )
                    Text(
                        text = "Requested",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = labelSize.value.sp
                        )
                    )
                }
            }
        }
    }
}
