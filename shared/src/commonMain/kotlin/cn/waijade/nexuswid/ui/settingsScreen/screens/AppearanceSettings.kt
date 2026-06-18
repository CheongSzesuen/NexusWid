package cn.waijade.nexuswid.ui.settingsScreen.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import cn.waijade.nexuswid.ui.mergePaddingValues
import cn.waijade.nexuswid.ui.settingsScreen.viewModel.SettingsAction
import cn.waijade.nexuswid.ui.settingsScreen.viewModel.SettingsState
import cn.waijade.nexuswid.ui.theme.CustomColors.detailPaneTopBarColors
import cn.waijade.nexuswid.ui.theme.CustomColors.listItemColors
import cn.waijade.nexuswid.ui.theme.CustomColors.switchColors
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.PANE_MAX_WIDTH
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.segmentedListItemShapes
import cn.waijade.nexuswid.ui.theme.NexusTheme
import cn.waijade.nexuswid.utils.toColor
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.appearance
import nexuswid.shared.generated.resources.back
import nexuswid.shared.generated.resources.black_theme
import nexuswid.shared.generated.resources.black_theme_desc
import nexuswid.shared.generated.resources.check
import nexuswid.shared.generated.resources.clear
import nexuswid.shared.generated.resources.color
import nexuswid.shared.generated.resources.color_scheme
import nexuswid.shared.generated.resources.colors
import nexuswid.shared.generated.resources.contrast
import nexuswid.shared.generated.resources.dark
import nexuswid.shared.generated.resources.ic_brightness_auto
import nexuswid.shared.generated.resources.dark_mode
import nexuswid.shared.generated.resources.dynamic
import nexuswid.shared.generated.resources.dynamic_color
import nexuswid.shared.generated.resources.dynamic_color_desc
import nexuswid.shared.generated.resources.light
import nexuswid.shared.generated.resources.light_mode
import nexuswid.shared.generated.resources.palette
import nexuswid.shared.generated.resources.settings
import nexuswid.shared.generated.resources.system_default
import nexuswid.shared.generated.resources.theme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceSettings(
    settingsState: SettingsState,
    onAction: (SettingsAction) -> Unit,
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

    val colorSchemes = remember {
        listOf(
            Color(0xfffeb4a7), Color(0xffffb3c0), Color(0xfffcaaff), Color(0xffb9c3ff),
            Color(0xff62d3ff), Color(0xff44d9f1), Color(0xff52dbc9), Color(0xff78dd77),
            Color(0xff9fd75c), Color(0xffc1d02d), Color(0xfffabd00), Color(0xffffb86e),
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
                    val themeMap = remember {
                        mapOf(
                            "auto" to Pair(
                                Res.drawable.ic_brightness_auto,
                                Res.string.system_default
                            ),
                            "light" to Pair(Res.drawable.light_mode, Res.string.light),
                            "dark" to Pair(Res.drawable.dark_mode, Res.string.dark)
                        )
                    }

                    SegmentedListItem(
                        onClick = {},
                        leadingContent = {
                            AnimatedContent(themeMap[settingsState.theme]!!.first) {
                                Icon(
                                    painter = painterResource(it),
                                    contentDescription = null,
                                )
                            }
                        },
                        content = { Text(stringResource(Res.string.theme)) },
                        supportingContent = {
                            val options = themeMap.toList()

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                options.fastForEachIndexed { index, entry ->
                                    val isSelected = settingsState.theme == entry.first
                                    ToggleButton(
                                        checked = isSelected,
                                        onCheckedChange = { onAction(SettingsAction.SaveTheme(entry.first)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics { role = Role.RadioButton },
                                        shapes =
                                            when (index) {
                                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                                options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                            },
                                    ) {
                                        Text(
                                            stringResource(entry.second.second),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(0, 3)
                    )
                }

                item {
                    val dynamicColor = settingsState.colorScheme == Color.White.toString()
                    SegmentedListItem(
                        onClick = {
                            onAction(
                                SettingsAction.SaveColorScheme(
                                    if (dynamicColor) colorSchemes.first()
                                    else Color.White
                                )
                            )
                        },
                        leadingContent = { Icon(painterResource(Res.drawable.colors), null) },
                        content = { Text(stringResource(Res.string.dynamic_color)) },
                        supportingContent = { Text(stringResource(Res.string.dynamic_color_desc)) },
                        trailingContent = {
                            Switch(
                                checked = dynamicColor,
                                onCheckedChange = {
                                    onAction(
                                        SettingsAction.SaveColorScheme(
                                            if (it) Color.White
                                            else colorSchemes.first()
                                        )
                                    )
                                },
                                thumbContent = {
                                    if (dynamicColor) {
                                        Icon(
                                            painter = painterResource(Res.drawable.check),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(Res.drawable.clear),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                },
                                colors = switchColors
                            )
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(1, 3)
                    )
                }

                item {
                    val dynamicColor = settingsState.colorScheme == Color.White.toString()
                    Box {
                        SegmentedListItem(
                            onClick = {},
                            leadingContent = {
                                Icon(
                                    painter = painterResource(Res.drawable.palette),
                                    contentDescription = null
                                )
                            },
                            content = { Text(stringResource(Res.string.color_scheme)) },
                            supportingContent = {
                                Text(
                                    if (dynamicColor) stringResource(Res.string.dynamic)
                                    else stringResource(Res.string.color)
                                )
                            },
                            colors = listItemColors,
                            shapes = ListItemDefaults.segmentedShapes(
                                1, 3,
                                ListItemDefaults.shapes(
                                    shape = MaterialTheme.shapes.extraSmall.copy(
                                        bottomStart = CornerSize(0),
                                        bottomEnd = CornerSize(0)
                                    )
                                )
                            )
                        )

                        Box(
                            Modifier
                                .matchParentSize()
                                .clickable(false) {}
                        )
                    }
                }

                item {
                    val currentColor = settingsState.colorScheme.toColor()
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 48.dp),
                        modifier = Modifier
                            .background(
                                listItemColors.containerColor,
                                shape = MaterialTheme.shapes.extraSmall.copy(
                                    topStart = CornerSize(0),
                                    topEnd = CornerSize(0)
                                )
                            )
                            .padding(bottom = 8.dp)
                    ) {
                        items(colorSchemes) { color ->
                            val isSelected = color == currentColor
                            IconButton(
                                shapes = IconButtonDefaults.shapes(),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = color,
                                ),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(48.dp),
                                onClick = {
                                    onAction(SettingsAction.SaveColorScheme(color))
                                }
                            ) {
                                AnimatedContent(isSelected) { selected ->
                                    when (selected) {
                                        true -> Icon(
                                            painterResource(Res.drawable.check),
                                            tint = Color.Black,
                                            contentDescription = null
                                        )
                                        else -> Unit
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SegmentedListItem(
                        onClick = { onAction(SettingsAction.SaveBlackTheme(!settingsState.blackTheme)) },
                        leadingContent = {
                            Icon(painterResource(Res.drawable.contrast), null)
                        },
                        content = { Text(stringResource(Res.string.black_theme)) },
                        supportingContent = { Text(stringResource(Res.string.black_theme_desc)) },
                        trailingContent = {
                            Switch(
                                checked = settingsState.blackTheme,
                                onCheckedChange = { onAction(SettingsAction.SaveBlackTheme(it)) },
                                thumbContent = {
                                    if (settingsState.blackTheme) {
                                        Icon(
                                            painter = painterResource(Res.drawable.check),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(Res.drawable.clear),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                },
                                colors = switchColors
                            )
                        },
                        colors = listItemColors,
                        shapes = segmentedListItemShapes(2, 3)
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
            settingsState = SettingsState(),
            onAction = {},
            contentPadding = PaddingValues(),
            onBack = {}
        )
    }
}
