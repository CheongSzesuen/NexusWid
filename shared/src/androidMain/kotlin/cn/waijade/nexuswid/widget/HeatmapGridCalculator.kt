package cn.waijade.nexuswid.widget

import kotlin.math.floor
import kotlin.math.min

object HeatmapGridCalculator {

    const val ROWS = 7
    const val MAX_COLS = 52
    const val GAP_DP = 2f
    const val MIN_CELL_DP = 4f
    const val MAX_CELL_DP = 64f
    const val COLUMN_REFERENCE_CELL_DP = 18f
    const val MIN_HEATMAP_HEIGHT_DP = 1f

    fun calculateColumns(widthDp: Float, heightDp: Float): Int {
        val safeWidth = widthDp.coerceAtLeast(1f)
        val safeHeight = heightDp.coerceAtLeast(MIN_HEATMAP_HEIGHT_DP)
        val cellByHeight = ((safeHeight - GAP_DP * (ROWS - 1)) / ROWS)
            .coerceAtLeast(MIN_CELL_DP)
        val columns = floor((safeWidth + GAP_DP) / (cellByHeight + GAP_DP)).toInt()
        return columns.coerceIn(1, MAX_COLS)
    }

    fun calculateColumns(widthDp: Float): Int {
        val safeWidth = widthDp.coerceAtLeast(1f)
        val columns = floor((safeWidth + GAP_DP) / (COLUMN_REFERENCE_CELL_DP + GAP_DP)).toInt()
        return columns.coerceIn(1, MAX_COLS)
    }

    fun calculateCellSize(widthDp: Float, heightDp: Float, columns: Int): Float {
        val safeWidth = widthDp.coerceAtLeast(1f)
        val safeHeight = heightDp.coerceAtLeast(MIN_HEATMAP_HEIGHT_DP)
        val safeCols = columns.coerceIn(1, MAX_COLS)

        val availableWidth = safeWidth - GAP_DP * (safeCols - 1)
        val availableHeight = safeHeight - GAP_DP * (ROWS - 1)
        return min(availableWidth / safeCols, availableHeight / ROWS)
            .coerceAtLeast(MIN_CELL_DP)
    }
}