package cn.waijade.nexuswid.data

import cn.waijade.nexuswid.data.github.PullRequestType

interface WidgetPreferences {
    var widgetHeatmapColorMode: HeatmapColorMode
    var widgetColorMode: HeatmapColorMode
    var weekStartsOnMonday: Boolean
    var liquidGlassBottomBar: Boolean
    var selectedPullRequestTypes: Set<PullRequestType>
}
