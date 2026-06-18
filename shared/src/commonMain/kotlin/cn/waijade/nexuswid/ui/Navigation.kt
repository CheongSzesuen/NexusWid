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
import nexuswid.shared.generated.resources.heatmap_color_mode
import nexuswid.shared.generated.resources.ic_github
import nexuswid.shared.generated.resources.palette
import nexuswid.shared.generated.resources.theme
import nexuswid.shared.generated.resources.week_start_day
import nexuswid.shared.generated.resources.widget
import nexuswid.shared.generated.resources.widget_contribution

val githubSettingsScreen = SettingsNavItem(
    Screen.Settings.GitHub,
    Res.drawable.ic_github,
    Res.string.github,
    listOf(Res.string.github_username, Res.string.github_token)
)

val settingsScreens = listOf(
    SettingsNavItem(
        Screen.Settings.Appearance,
        Res.drawable.palette,
        Res.string.appearance,
        listOf(Res.string.theme, Res.string.color_scheme, Res.string.black_theme)
    ),
    SettingsNavItem(
        Screen.Settings.Widget,
        Res.drawable.palette,
        Res.string.widget,
        listOf(Res.string.widget_contribution, Res.string.heatmap_color_mode, Res.string.week_start_day)
    )
)
