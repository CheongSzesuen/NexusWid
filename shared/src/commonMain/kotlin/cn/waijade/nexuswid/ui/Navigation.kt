package cn.waijade.nexuswid

import cn.waijade.nexuswid.ui.Screen
import cn.waijade.nexuswid.ui.SettingsNavItem
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.appearance
import nexuswid.shared.generated.resources.black_theme
import nexuswid.shared.generated.resources.color_scheme
import nexuswid.shared.generated.resources.github
import nexuswid.shared.generated.resources.github_token
import nexuswid.shared.generated.resources.github_username
import nexuswid.shared.generated.resources.palette
import nexuswid.shared.generated.resources.theme
import nexuswid.shared.generated.resources.widget
import nexuswid.shared.generated.resources.widget_contribution

val settingsScreens = listOf(
    SettingsNavItem(
        Screen.Settings.Appearance,
        Res.drawable.palette,
        Res.string.appearance,
        listOf(Res.string.theme, Res.string.color_scheme, Res.string.black_theme)
    ),
    SettingsNavItem(
        Screen.Settings.GitHub,
        Res.drawable.palette,
        Res.string.github,
        listOf(Res.string.github_username, Res.string.github_token)
    ),
    SettingsNavItem(
        Screen.Settings.Widget,
        Res.drawable.palette,
        Res.string.widget,
        listOf(Res.string.widget_contribution)
    )
)
