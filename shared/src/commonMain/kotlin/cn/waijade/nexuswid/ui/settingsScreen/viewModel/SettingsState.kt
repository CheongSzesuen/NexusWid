package cn.waijade.nexuswid.ui.settingsScreen.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.github.IssueType
import cn.waijade.nexuswid.data.github.PullRequestType

@Immutable
data class SettingsState(
    val theme: String = "auto",
    val colorScheme: String = Color.White.toString(),
    val blackTheme: Boolean = false,
    val heatmapColorMode: HeatmapColorMode = HeatmapColorMode.SYSTEM,
    val widgetColorMode: HeatmapColorMode = HeatmapColorMode.SYSTEM,
    val weekStartsOnMonday: Boolean = false,
    val liquidGlassBottomBar: Boolean = false,
    val selectedPullRequestTypes: Set<PullRequestType> = setOf(PullRequestType.REVIEW_REQUESTED),
    val selectedIssueTypes: Set<IssueType> = setOf(IssueType.ASSIGNED),
    val isAtRoot: Boolean = true
)
