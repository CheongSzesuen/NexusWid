package cn.waijade.nexuswid.ui.settingsScreen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.detailPane
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.listPane
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import cn.waijade.nexuswid.ui.Screen
import cn.waijade.nexuswid.ui.calculatePaneScaffoldDirective
import cn.waijade.nexuswid.ui.settingsScreen.screens.AboutScreen
import cn.waijade.nexuswid.ui.settingsScreen.screens.AppearanceSettings
import cn.waijade.nexuswid.ui.settingsScreen.screens.BuildInfoScreen
import cn.waijade.nexuswid.ui.settingsScreen.screens.GitHubSettingsScreen
import cn.waijade.nexuswid.ui.settingsScreen.screens.SettingsMainScreen
import cn.waijade.nexuswid.ui.settingsScreen.screens.WidgetSettingsScreen
import cn.waijade.nexuswid.ui.settingsScreen.viewModel.SettingsAction
import cn.waijade.nexuswid.ui.settingsScreen.viewModel.SettingsViewModel
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.utils.onBack
import cn.waijade.nexuswid.ui.utils.onTopLevelNavigate
import cn.waijade.nexuswid.widget.ContributionHeatmapWidgetProvider

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SettingsScreenRoot(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel
) {
    val backStack = settingsViewModel.backStack
    val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()
    val directionMultiplier = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 1 else -1
    val context = LocalContext.current

    NavDisplay(
        backStack = backStack,
        onBack = backStack::onBack,
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { directionMultiplier * it }))
                .togetherWith(slideOutHorizontally(targetOffsetX = { directionMultiplier * -it / 4 }) + fadeOut())
        },
        popTransitionSpec = {
            (slideInHorizontally(initialOffsetX = { directionMultiplier * -it / 4 }) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { directionMultiplier * it }))
        },
        predictivePopTransitionSpec = {
            (slideInHorizontally(initialOffsetX = { directionMultiplier * -it / 4 }) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { directionMultiplier * it }))
        },
        sceneStrategies = listOf(
            rememberListDetailSceneStrategy(
                directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
            )
        ),
        entryProvider = entryProvider {
            entry<Screen.Settings.Main>(
                metadata = listPane(detailPlaceholder = { DetailPlaceholder(Icons.Default.Settings) })
            ) {
                SettingsMainScreen(
                    contentPadding = contentPadding,
                    currentScreen = backStack.last(),
                    onNavigate = backStack::onTopLevelNavigate,
                    modifier = modifier,
                )
            }

            entry<Screen.Settings.About>(
                metadata = detailPane()
            ) {
                AboutScreen(
                    contentPadding = contentPadding,
                    onBack = backStack::onBack,
                    onNavigate = backStack::onTopLevelNavigate
                )
            }

            entry<Screen.Settings.BuildInfo>(
                metadata = detailPane()
            ) {
                BuildInfoScreen(
                    contentPadding = contentPadding,
                    onBack = backStack::onBack
                )
            }

            entry<Screen.Settings.Appearance>(
                metadata = detailPane()
            ) {
                AppearanceSettings(
                    settingsState = settingsState,
                    onAction = settingsViewModel::onAction,
                    contentPadding = contentPadding,
                    onBack = backStack::onBack,
                    modifier = modifier,
                )
            }

            entry<Screen.Settings.GitHub>(
                metadata = detailPane()
            ) {
                GitHubSettingsScreen(
                    contentPadding = contentPadding,
                    onBack = backStack::onBack,
                    modifier = modifier,
                )
            }

            entry<Screen.Settings.Widget>(
                metadata = detailPane()
            ) {
                WidgetSettingsScreen(
                    contentPadding = contentPadding,
                    onBack = backStack::onBack,
                    heatmapColorMode = settingsState.heatmapColorMode,
                    onHeatmapColorModeChange = { mode ->
                        settingsViewModel.onAction(SettingsAction.SaveHeatmapColorMode(mode))
                        ContributionHeatmapWidgetProvider.updateAll(context)
                    },
                    weekStartsOnMonday = settingsState.weekStartsOnMonday,
                    onWeekStartsOnMondayChange = { enabled ->
                        settingsViewModel.onAction(SettingsAction.SaveWeekStartsOnMonday(enabled))
                        ContributionHeatmapWidgetProvider.updateAll(context)
                    },
                    modifier = modifier,
                )
            }
        },
        modifier = Modifier.background(topBarColors.containerColor)
    )
}
