package cn.waijade.nexuswid.ui.settingsScreen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.stringResource
import cn.waijade.nexuswid.ui.mergePaddingValues
import cn.waijade.nexuswid.ui.settingsScreen.SettingsSwitchItem
import cn.waijade.nexuswid.ui.theme.CustomColors.detailPaneTopBarColors
import cn.waijade.nexuswid.ui.theme.CustomColors.listItemColors
import cn.waijade.nexuswid.ui.theme.CustomColors.switchColors
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.PANE_MAX_WIDTH
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.segmentedListItemShapes
import cn.waijade.nexuswid.ui.theme.NexusTheme
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.appearance
import nexuswid.shared.generated.resources.back
import nexuswid.shared.generated.resources.black_theme
import nexuswid.shared.generated.resources.black_theme_desc
import nexuswid.shared.generated.resources.settings
import nexuswid.shared.generated.resources.system_default

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceSettings(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val barColors = if (widthExpanded) detailPaneTopBarColors
    else topBarColors

    var theme by remember { mutableStateOf("auto") }
    var blackThemeEnabled by remember { mutableStateOf(false) }

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
                            stringResource(Res.string.appearance),
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
                                    Icons.Default.ArrowBack,
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
                    Spacer(Modifier.height(14.dp))
                }
                item {
                    SegmentedListItem(
                        onClick = {},
                        leadingContent = {
                            Icon(Icons.Default.Star, null)
                        },
                        content = { Text(stringResource(Res.string.system_default)) },
                        shapes = segmentedListItemShapes(0, 2),
                        colors = listItemColors
                    )
                }
                item {
                    val switchItem = SettingsSwitchItem(
                        checked = blackThemeEnabled,
                        icon = Icons.Default.Favorite,
                        label = stringResource(Res.string.black_theme),
                        description = stringResource(Res.string.black_theme_desc),
                        onClick = { blackThemeEnabled = it }
                    )
                    SegmentedListItem(
                        onClick = { switchItem.onClick(!switchItem.checked) },
                        leadingContent = {
                            Icon(switchItem.icon, null)
                        },
                        content = { Text(switchItem.label) },
                        supportingContent = { Text(switchItem.description) },
                        trailingContent = {
                            Switch(
                                checked = switchItem.checked,
                                onCheckedChange = { switchItem.onClick(it) },
                                thumbContent = {
                                    if (switchItem.checked) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                },
                                colors = switchColors
                            )
                        },
                        colors = listItemColors,
                        enabled = true,
                        shapes = segmentedListItemShapes(1, 2)
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Preview
@Composable
fun AppearanceSettingsPreview() {
    NexusTheme(dynamicColor = false) {
        AppearanceSettings(
            contentPadding = PaddingValues(),
            onBack = {}
        )
    }
}
