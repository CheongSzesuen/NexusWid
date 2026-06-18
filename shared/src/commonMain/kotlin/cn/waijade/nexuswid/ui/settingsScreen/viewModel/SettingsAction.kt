package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.ui.graphics.Color
import cn.waijade.nexuswid.data.HeatmapAccent

sealed interface SettingsAction {
    data class SaveTheme(val theme: String) : SettingsAction
    data class SaveColorScheme(val color: Color) : SettingsAction
    data class SaveBlackTheme(val enabled: Boolean) : SettingsAction
    data class SaveHeatmapAccent(val accent: HeatmapAccent) : SettingsAction
}
