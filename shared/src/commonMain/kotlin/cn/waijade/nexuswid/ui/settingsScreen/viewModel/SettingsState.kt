package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import cn.waijade.nexuswid.data.HeatmapAccent

@Immutable
data class SettingsState(
    val theme: String = "auto",
    val colorScheme: String = Color.White.toString(),
    val blackTheme: Boolean = false,
    val heatmapAccent: HeatmapAccent = HeatmapAccent.GITHUB,
    val weekStartsOnMonday: Boolean = false
)
