package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import cn.waijade.nexuswid.ui.Screen

class SettingsViewModel : ViewModel() {
    val backStack = mutableStateListOf<Screen.Settings>(Screen.Settings.Main)

    private var _theme = "auto"
    private var _colorScheme = "white"
    private var _blackTheme = false

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SaveTheme -> saveTheme(action.theme)
            is SettingsAction.SaveColorScheme -> saveColorScheme(action.color)
            is SettingsAction.SaveBlackTheme -> saveBlackTheme(action.enabled)
        }
    }

    private fun saveTheme(theme: String) {
        _theme = theme
    }

    private fun saveColorScheme(color: Color) {
        _colorScheme = color.toString()
    }

    private fun saveBlackTheme(enabled: Boolean) {
        _blackTheme = enabled
    }
}
