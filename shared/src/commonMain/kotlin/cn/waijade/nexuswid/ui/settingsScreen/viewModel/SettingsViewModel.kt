package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.waijade.nexuswid.data.HeatmapAccent
import cn.waijade.nexuswid.data.StateRepository
import cn.waijade.nexuswid.ui.Screen
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val stateRepository: StateRepository
) : ViewModel() {
    val backStack = mutableStateListOf<Screen.Settings>(Screen.Settings.Main)

    val settingsState = stateRepository.settingsState.asStateFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SaveTheme -> saveTheme(action.theme)
            is SettingsAction.SaveColorScheme -> saveColorScheme(action.color)
            is SettingsAction.SaveBlackTheme -> saveBlackTheme(action.enabled)
            is SettingsAction.SaveHeatmapAccent -> saveHeatmapAccent(action.accent)
        }
    }

    private fun saveTheme(theme: String) {
        viewModelScope.launch {
            stateRepository.updateTheme(theme)
        }
    }

    private fun saveColorScheme(color: Color) {
        viewModelScope.launch {
            stateRepository.updateColorScheme(color.toString())
        }
    }

    private fun saveBlackTheme(enabled: Boolean) {
        viewModelScope.launch {
            stateRepository.updateBlackTheme(enabled)
        }
    }

    private fun saveHeatmapAccent(accent: HeatmapAccent) {
        viewModelScope.launch {
            stateRepository.updateHeatmapAccent(accent)
        }
    }
}
