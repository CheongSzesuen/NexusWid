package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import cn.waijade.nexuswid.data.HeatmapColorMode

@Immutable
data class SettingsState(
    val theme: String = "auto",
    val colorScheme: String = Color.White.toString(),
    val blackTheme: Boolean = false,
    val heatmapColorMode: HeatmapColorMode = HeatmapColorMode.SYSTEM,
    val weekStartsOnMonday: Boolean = false
)
