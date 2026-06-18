package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsState(
    val theme: String = "auto",
    val colorScheme: String = "white",
    val blackTheme: Boolean = false,
)
