package cn.waijade.nexuswid

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import cn.waijade.nexuswid.ui.Screen
import cn.waijade.nexuswid.ui.SettingsNavItem
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.appearance
import nexuswid.shared.generated.resources.black_theme
import nexuswid.shared.generated.resources.color_scheme
import nexuswid.shared.generated.resources.theme

val settingsScreens = listOf(
    SettingsNavItem(
        Screen.Settings.Appearance,
        Icons.Default.Star,
        Res.string.appearance,
        listOf(Res.string.theme, Res.string.color_scheme, Res.string.black_theme)
    )
)
