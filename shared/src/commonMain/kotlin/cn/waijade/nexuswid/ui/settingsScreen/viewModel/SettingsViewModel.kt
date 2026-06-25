package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.StateRepository
import cn.waijade.nexuswid.data.WidgetPreferences
import cn.waijade.nexuswid.data.github.IssueType
import cn.waijade.nexuswid.data.github.PullRequestType
import cn.waijade.nexuswid.ui.Screen
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val stateRepository: StateRepository,
    private val widgetPreferences: WidgetPreferences
) : ViewModel() {
    val backStack = mutableStateListOf<Screen.Settings>(Screen.Settings.Main)

    val settingsState = stateRepository.settingsState.asStateFlow()

    init {
        stateRepository.updateHeatmapColorMode(widgetPreferences.widgetHeatmapColorMode)
        stateRepository.updateWidgetColorMode(widgetPreferences.widgetColorMode)
        stateRepository.updateWeekStartsOnMonday(widgetPreferences.weekStartsOnMonday)
        stateRepository.updateLiquidGlassBottomBar(widgetPreferences.liquidGlassBottomBar)
        stateRepository.updateSelectedPullRequestTypes(widgetPreferences.selectedPullRequestTypes)
        stateRepository.updateSelectedIssueTypes(widgetPreferences.selectedIssueTypes)
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SaveTheme -> saveTheme(action.theme)
            is SettingsAction.SaveColorScheme -> saveColorScheme(action.color)
            is SettingsAction.SaveBlackTheme -> saveBlackTheme(action.enabled)
            is SettingsAction.SaveHeatmapColorMode -> saveHeatmapColorMode(action.mode)
            is SettingsAction.SaveWidgetColorMode -> saveWidgetColorMode(action.mode)
            is SettingsAction.SaveWeekStartsOnMonday -> saveWeekStartsOnMonday(action.enabled)
            is SettingsAction.SaveLiquidGlassBottomBar -> saveLiquidGlassBottomBar(action.enabled)
            is SettingsAction.SavePullRequestTypes -> savePullRequestTypes(action.types)
            is SettingsAction.SaveIssueTypes -> saveIssueTypes(action.types)
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

    private fun saveHeatmapColorMode(mode: HeatmapColorMode) {
        viewModelScope.launch {
            stateRepository.updateHeatmapColorMode(mode)
            widgetPreferences.widgetHeatmapColorMode = mode
        }
    }

    private fun saveWidgetColorMode(mode: HeatmapColorMode) {
        viewModelScope.launch {
            stateRepository.updateWidgetColorMode(mode)
            widgetPreferences.widgetColorMode = mode
        }
    }

    private fun saveWeekStartsOnMonday(enabled: Boolean) {
        viewModelScope.launch {
            stateRepository.updateWeekStartsOnMonday(enabled)
            widgetPreferences.weekStartsOnMonday = enabled
        }
    }

    private fun saveLiquidGlassBottomBar(enabled: Boolean) {
        viewModelScope.launch {
            stateRepository.updateLiquidGlassBottomBar(enabled)
            widgetPreferences.liquidGlassBottomBar = enabled
        }
    }

    private fun savePullRequestTypes(types: Set<PullRequestType>) {
        viewModelScope.launch {
            stateRepository.updateSelectedPullRequestTypes(types)
            widgetPreferences.selectedPullRequestTypes = types
        }
    }

    private fun saveIssueTypes(types: Set<IssueType>) {
        viewModelScope.launch {
            stateRepository.updateSelectedIssueTypes(types)
            widgetPreferences.selectedIssueTypes = types
        }
    }

    fun updateIsAtRoot(isAtRoot: Boolean) {
        viewModelScope.launch {
            stateRepository.updateIsAtRoot(isAtRoot)
        }
    }
}
