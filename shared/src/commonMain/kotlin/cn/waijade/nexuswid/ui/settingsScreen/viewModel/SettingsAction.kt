package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.ui.graphics.Color

sealed interface SettingsAction {
    data class SaveTheme(val theme: String) : SettingsAction
    data class SaveColorScheme(val color: Color) : SettingsAction
    data class SaveBlackTheme(val enabled: Boolean) : SettingsAction
}
