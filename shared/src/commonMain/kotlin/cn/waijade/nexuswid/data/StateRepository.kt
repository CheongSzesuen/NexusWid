package cn.waijade.nexuswid.data

import cn.waijade.nexuswid.data.github.PullRequestType
import cn.waijade.nexuswid.ui.settingsScreen.viewModel.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class StateRepository {
    val settingsState = MutableStateFlow(SettingsState())

    fun updateTheme(theme: String) {
        settingsState.update { it.copy(theme = theme) }
    }

    fun updateColorScheme(colorScheme: String) {
        settingsState.update { it.copy(colorScheme = colorScheme) }
    }

    fun updateBlackTheme(blackTheme: Boolean) {
        settingsState.update { it.copy(blackTheme = blackTheme) }
    }

    fun updateHeatmapColorMode(mode: HeatmapColorMode) {
        settingsState.update { it.copy(heatmapColorMode = mode) }
    }

    fun updateWeekStartsOnMonday(enabled: Boolean) {
        settingsState.update { it.copy(weekStartsOnMonday = enabled) }
    }

    fun updateLiquidGlassBottomBar(enabled: Boolean) {
        settingsState.update { it.copy(liquidGlassBottomBar = enabled) }
    }

    fun updateSelectedPullRequestTypes(types: Set<PullRequestType>) {
        settingsState.update { it.copy(selectedPullRequestTypes = types) }
    }
}
