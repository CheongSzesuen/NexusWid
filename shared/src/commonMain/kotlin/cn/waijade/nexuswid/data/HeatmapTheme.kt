package cn.waijade.nexuswid.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HeatmapPalette(
    val containerBg: Color,
    val level0: Color,
    val level1: Color,
    val level2: Color,
    val level3: Color,
    val level4: Color,
    val border: Color
) {
    fun toIntArray(): IntArray = intArrayOf(
        level0.toArgb(),
        level1.toArgb(),
        level2.toArgb(),
        level3.toArgb(),
        level4.toArgb()
    )
}

enum class HeatmapTheme(
    val light: HeatmapPalette,
    val dark: HeatmapPalette
) {
    DEFAULT(
        light = HeatmapPalette(
            containerBg = Color(0xFFF6F8FA),
            level0 = Color(0xFFEBEDF0),
            level1 = Color(0xFF9BE9A8),
            level2 = Color(0xFF40C463),
            level3 = Color(0xFF30A14E),
            level4 = Color(0xFF216E39),
            border = Color(0x0D1F2328)
        ),
        dark = HeatmapPalette(
            containerBg = Color(0xFF0D1117),
            level0 = Color(0xFF161B22),
            level1 = Color(0xFF0E4429),
            level2 = Color(0xFF006D32),
            level3 = Color(0xFF26A641),
            level4 = Color(0xFF39D353),
            border = Color(0x0DFFFFFF)
        )
    ),
    HALLOWEEN(
        light = HeatmapPalette(
            containerBg = Color(0xFFF6F8FA),
            level0 = Color(0xFFEBEDF0),
            level1 = Color(0xFFF0DB3D),
            level2 = Color(0xFFFFD642),
            level3 = Color(0xFFF68C41),
            level4 = Color(0xFF1F2328),
            border = Color(0x0D1F2328)
        ),
        dark = HeatmapPalette(
            containerBg = Color(0xFF0D1117),
            level0 = Color(0xFF161B22),
            level1 = Color(0xFFFAC68F),
            level2 = Color(0xFFC46212),
            level3 = Color(0xFF984B10),
            level4 = Color(0xFFE3D04F),
            border = Color(0x0DFFFFFF)
        )
    ),
    WINTER(
        light = HeatmapPalette(
            containerBg = Color(0xFFF6F8FA),
            level0 = Color(0xFFEBEDF0),
            level1 = Color(0xFFB6E3FF),
            level2 = Color(0xFF54AEFF),
            level3 = Color(0xFF0969DA),
            level4 = Color(0xFF0A3069),
            border = Color(0x0D1F2328)
        ),
        dark = HeatmapPalette(
            containerBg = Color(0xFF0D1117),
            level0 = Color(0xFF161B22),
            level1 = Color(0xFF0C2D6B),
            level2 = Color(0xFF1158C7),
            level3 = Color(0xFF58A6FF),
            level4 = Color(0xFFCAE8FF),
            border = Color(0x0DFFFFFF)
        )
    );

    fun palette(isDark: Boolean): HeatmapPalette = if (isDark) dark else light

    companion object {
        fun resolveCurrentTheme(): HeatmapTheme {
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(timeZone).date

            return when {
                isHalloweenSeason(today) -> HALLOWEEN
                isWinterSeason(today) -> WINTER
                else -> DEFAULT
            }
        }

        private fun isHalloweenSeason(date: LocalDate): Boolean {
            val year = date.year
            val start = LocalDate(year, 10, 25)
            val end = LocalDate(year, 11, 3)
            return date in start..end
        }

        private fun isWinterSeason(date: LocalDate): Boolean {
            val year = date.year
            val winterStart = LocalDate(year, 12, 1)
            val winterEnd = LocalDate(year + 1, 1, 6)
            val winterStartPrev = LocalDate(year - 1, 12, 1)
            val winterEndCurr = LocalDate(year, 1, 6)

            return date in winterStart..winterEnd || date in winterStartPrev..winterEndCurr
        }
    }
}
