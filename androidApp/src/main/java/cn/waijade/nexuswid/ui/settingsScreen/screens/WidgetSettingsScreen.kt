@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package cn.waijade.nexuswid.ui.settingsScreen.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.waijade.nexuswid.widget.ContributionHeatmapWidgetProvider
import cn.waijade.nexuswid.widget.HeatmapGridCalculator
import cn.waijade.nexuswid.ui.mergePaddingValues
import kotlin.random.Random

private const val PREVIEW_CONTENT_PADDING_DP = 6f
private const val PREVIEW_CELL_SIZE_SCALE = 1f

@Composable
fun WidgetSettingsScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var previewFrameWidthPx by rememberSaveable { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("小组件设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = insets.calculateTopPadding() + 16.dp,
                bottom = insets.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "贡献热图",
                            style = MaterialTheme.typography.titleMedium
                        )
                        FilledTonalButton(
                            onClick = {
                                val message = when (requestPinContributionHeatmapWidget(context)) {
                                    PinWidgetRequestResult.REQUESTED -> "请在系统弹窗中确认添加小组件"
                                    PinWidgetRequestResult.NOT_SUPPORTED -> "当前桌面不支持一键添加小组件"
                                    PinWidgetRequestResult.UNSUPPORTED_ANDROID -> "当前安卓版本不支持一键添加"
                                    PinWidgetRequestResult.FAILED -> "添加请求发送失败，请手动添加"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("添加到桌面")
                        }
                    }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                            previewFrameWidthPx.coerceIn(effectiveMinWidthPx, maxWidthPx)
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
                                                            .coerceIn(effectiveMinWidthPx, maxWidthPx)
                                                }
                                            }
                                    ) {
                                        HeatmapPreviewCard(
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "拖动预览边框可调节宽度，列数会随宽度自动变化",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

@Composable
private fun HeatmapPreviewCard(
    modifier: Modifier = Modifier
) {
    val surface = MaterialTheme.colorScheme.surfaceContainerLow
    val outline = MaterialTheme.colorScheme.outlineVariant
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val levels = remember(empty) {
        listOf(
            empty,
            Color(0xFF9BE9A8),
            Color(0xFF40C463),
            Color(0xFF30A14E),
            Color(0xFF216E39)
        )
    }
    val previewSeed = rememberSaveable { Random.nextInt() }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = outline
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val rows = HeatmapGridCalculator.ROWS
            val contentHorizontalInset = (1f + PREVIEW_CONTENT_PADDING_DP) * 2f
            val contentVerticalInset = (1f + PREVIEW_CONTENT_PADDING_DP) * 2f
            val contentWidthDp = (maxWidth.value - contentHorizontalInset).coerceAtLeast(1f)
            val contentHeightDp = (maxHeight.value - contentVerticalInset).coerceAtLeast(1f)
            val heatmapAreaHeightDp = contentHeightDp.coerceAtLeast(1f)
            val columns = remember(contentWidthDp, contentHeightDp) {
                HeatmapGridCalculator.calculateColumns(
                    widthDp = contentWidthDp,
                    heightDp = heatmapAreaHeightDp
                )
            }
            val cellSize: Dp = remember(contentWidthDp, contentHeightDp, columns) {
                HeatmapGridCalculator.calculateCellSize(
                    widthDp = contentWidthDp,
                    heightDp = heatmapAreaHeightDp,
                    columns = columns
                ).times(PREVIEW_CELL_SIZE_SCALE).dp
            }
            val gap = HeatmapGridCalculator.GAP_DP.dp
            val sample = remember(previewSeed, columns) {
                val random = Random(previewSeed)
                List(columns * rows) { random.nextInt(5) }
            }
            val gridWidth = cellSize * columns + gap * (columns - 1)
            val gridHeight = cellSize * rows + gap * (rows - 1)

            Surface(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize(),
                shape = RoundedCornerShape(15.dp),
                color = surface
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
                                    val level = sample[index].coerceIn(0, 4)
                                    val color = levels[level]
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