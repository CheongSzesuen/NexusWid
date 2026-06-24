package cn.waijade.nexuswid.ui

import cn.waijade.nexuswid.data.github.PullRequestItem
import cn.waijade.nexuswid.data.github.PullRequestType

data class HomeWidgetData(
    val contributionLevels: Map<String, Int> = emptyMap(),
    val prItems: List<PullRequestItem> = emptyList(),
    val prTypes: Set<PullRequestType> = emptySet(),
    val reviewsCount: Int = -1,
    val isHeatmapLoaded: Boolean = false,
    val isPrLoaded: Boolean = false,
    val isReviewsLoaded: Boolean = false,
)
