package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.ui.graphics.Color
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.github.PullRequestType

sealed interface SettingsAction {
    data class SaveTheme(val theme: String) : SettingsAction
    data class SaveColorScheme(val color: Color) : SettingsAction
    data class SaveBlackTheme(val enabled: Boolean) : SettingsAction
    data class SaveHeatmapColorMode(val mode: HeatmapColorMode) : SettingsAction
    data class SaveWeekStartsOnMonday(val enabled: Boolean) : SettingsAction
    data class SaveLiquidGlassBottomBar(val enabled: Boolean) : SettingsAction
    data class SavePullRequestTypes(val types: Set<PullRequestType>) : SettingsAction
}
