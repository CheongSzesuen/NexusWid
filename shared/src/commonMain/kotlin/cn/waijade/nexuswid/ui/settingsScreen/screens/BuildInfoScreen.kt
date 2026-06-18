package cn.waijade.nexuswid.ui.settingsScreen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import cn.waijade.nexuswid.ui.mergePaddingValues
import cn.waijade.nexuswid.ui.theme.CustomColors.listItemColors
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.PANE_MAX_WIDTH
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.segmentedListItemShapes
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.back
import nexuswid.shared.generated.resources.build_info

private data class BuildDetailItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BuildInfoScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val barColors = topBarColors

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
                            stringResource(Res.string.build_info),
                            fontFamily = LocalAppFonts.current.topBarTitle
                        )
                    },
                    subtitle = {
                        Text(stringResource(Res.string.build_info))
                    },
                    navigationIcon = {
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = insets,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(innerPadding.calculateTopPadding() + 16.dp)) }

                item { BuildConfigSection() }
                item { PlatformSection() }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun BuildConfigSection() {
    Column {
        SectionLabel("构建配置")
        BuildInfoDetailCard(
            items = listOf(
                BuildDetailItem("Commit", "a1b2c3d", Icons.Rounded.Code),
                BuildDetailItem("版本名称", "v0.0.1", Icons.Rounded.Info),
                BuildDetailItem("版本代码", "1", Icons.Rounded.Tag),
                BuildDetailItem("构建类型", "Debug", Icons.Rounded.Build),
                BuildDetailItem("构建时间 (UTC+8)", "2026-06-18 14:30:00", Icons.Rounded.Schedule),
                BuildDetailItem("构建者", "WaiJade", Icons.Rounded.Person),
                BuildDetailItem("构建分支", "main", Icons.AutoMirrored.Rounded.CallSplit),
            )
        )
    }
}

private fun apiToAndroidVersion(api: Int): String = when (api) {
    23 -> "6.0"
    24 -> "7.0"
    25 -> "7.1"
    26 -> "8.0"
    27 -> "8.1"
    28 -> "9"
    29 -> "10"
    30 -> "11"
    31 -> "12"
    32 -> "12L"
    33 -> "13"
    34 -> "14"
    35 -> "15"
    36 -> "16"
    37 -> "17"
    else -> api.toString()
}

@Composable
private fun PlatformSection() {
    Column {
        SectionLabel("平台与环境")
        BuildInfoDetailCard(
            items = listOf(
                BuildDetailItem("最低安卓版本", apiToAndroidVersion(26), Icons.Rounded.SdStorage),
                BuildDetailItem("目标安卓版本", apiToAndroidVersion(37), Icons.Rounded.SdStorage),
                BuildDetailItem("编译 SDK", "37", Icons.Rounded.SdStorage),
                BuildDetailItem("AGP", "9.1.1", Icons.Rounded.Build),
                BuildDetailItem("Kotlin", "2.3.20", Icons.Rounded.Code),
                BuildDetailItem("Gradle", "9.3.1", Icons.Rounded.Build),
                BuildDetailItem("Compose BOM", "2026.05.00", Icons.Rounded.Info),
                BuildDetailItem("JDK", "17.0.1", Icons.Rounded.Code),
                BuildDetailItem("包名", "cn.waijade.nexuswid", Icons.Rounded.Info),
                BuildDetailItem("构建设备", "Linux / amd64", Icons.Rounded.Computer),
            )
        )
    }
}

@Composable
private fun BuildInfoDetailCard(
    items: List<BuildDetailItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEachIndexed { index, item ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = listItemColors.containerColor,
                ),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
