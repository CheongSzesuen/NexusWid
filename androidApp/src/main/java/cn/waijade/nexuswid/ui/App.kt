package cn.waijade.nexuswid.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import org.jetbrains.compose.resources.stringResource
import cn.waijade.nexuswid.ui.settingsScreen.SettingsScreenRoot
import cn.waijade.nexuswid.ui.theme.NexusTheme
import cn.waijade.nexuswid.ui.utils.onBack
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.home
import nexuswid.shared.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App() {
    NexusTheme {
        AppScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    val motionScheme = motionScheme
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()

    val backStack = rememberNavBackStack(Screen.Home)
    val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        FloatingToolbarExitDirection.Bottom
    )

    val mainScreens = remember {
        listOf(
            NavItem(
                Screen.Home,
                Icons.Outlined.Home,
                Icons.Filled.Home,
                Res.string.home
            ) {},
            NavItem(
                Screen.Settings.Main,
                Icons.Outlined.Settings,
                Icons.Filled.Settings,
                Res.string.settings
            ) {}
        )
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                true,
                enter = slideInVertically(motionScheme.slowSpatialSpec()) { it },
                exit = slideOutVertically(motionScheme.slowSpatialSpec()) { it }
            ) {
                val wide = remember {
                    windowSizeClass.isWidthAtLeastBreakpoint(
                        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = cutoutInsets.calculateStartPadding(layoutDirection),
                            end = cutoutInsets.calculateEndPadding(layoutDirection)
                        ),
                    Alignment.Center
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        scrollBehavior = toolbarScrollBehavior,
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                            toolbarContainerColor = colorScheme.primaryContainer,
                            toolbarContentColor = colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .padding(
                                top = ScreenOffset,
                                bottom = systemBarsInsets.calculateBottomPadding()
                                        + ScreenOffset
                            )
                            .zIndex(1f)
                    ) {
                        mainScreens.fastForEach { item ->
                            val selected by remember { derivedStateOf { backStack.lastOrNull() == item.route } }
                            TooltipBox(
                                positionProvider =
                                    TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Above
                                    ),
                                tooltip = { PlainTooltip { Text(stringResource(item.label)) } },
                                state = rememberTooltipState()
                            ) {
                                ToggleButton(
                                    checked = selected,
                                    onCheckedChange = if (!selected) {
                                        {
                                            if (backStack.size < 2) backStack.add(item.route)
                                            else backStack[1] = item.route
                                        }
                                    } else {
                                        { item.onNavigateHome() }
                                    },
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = colorScheme.primaryContainer,
                                        contentColor = colorScheme.onPrimaryContainer,
                                        checkedContainerColor = colorScheme.primary,
                                        checkedContentColor = colorScheme.onPrimary
                                    ),
                                    shapes = ToggleButtonDefaults.shapes(
                                        CircleShape,
                                        CircleShape,
                                        CircleShape
                                    ),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Crossfade(selected) {
                                            if (it) Icon(
                                                item.selectedIcon,
                                                stringResource(item.label)
                                            )
                                            else Icon(
                                                item.unselectedIcon,
                                                stringResource(item.label)
                                            )
                                        }
                                        AnimatedVisibility(
                                            visible = selected || wide,
                                            enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                                            exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                                        ) {
                                            Text(
                                                text = stringResource(item.label),
                                                fontSize = 16.sp,
                                                lineHeight = 24.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Clip,
                                                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { contentPadding ->
        SharedTransitionLayout {
            NavDisplay(
                backStack = backStack,
                onBack = backStack::onBack,
                transitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                popTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                predictivePopTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                entryProvider = entryProvider {
                    entry<Screen.Home> {
                        HomeScreen(
                            contentPadding = contentPadding,
                        )
                    }

                    entry<Screen.Settings.Main> {
                        SettingsScreenRoot(
                            contentPadding = contentPadding
                        )
                    }
                },
            )
        }
    }
}
