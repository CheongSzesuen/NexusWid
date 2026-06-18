package cn.waijade.nexuswid.ui.settingsScreen

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingsSwitchItem(
    val checked: Boolean,
    val enabled: Boolean = true,
    val icon: ImageVector,
    val label: String,
    val description: String,
    val onClick: (Boolean) -> Unit
)
