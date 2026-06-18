package cn.waijade.nexuswid.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class HeatmapTheme(
    val level0: Color,
    val level1: Color,
    val level2: Color,
    val level3: Color,
    val level4: Color
) {
    DEFAULT(
        level0 = Color(0xFF2A313C),
        level1 = Color(0xFF1B4721),
        level2 = Color(0xFF2B6A30),
        level3 = Color(0xFF46954A),
        level4 = Color(0xFF6BC46D)
    ),
    HALLOWEEN(
        level0 = Color(0xFF2A313C),
        level1 = Color(0xFFFAC68F),
        level2 = Color(0xFFC46212),
        level3 = Color(0xFF984B10),
        level4 = Color(0xFFE3D04F)
    ),
    WINTER(
        level0 = Color(0xFF2A313C),
        level1 = Color(0xFF143D79),
        level2 = Color(0xFF255AB2),
        level3 = Color(0xFF539BF5),
        level4 = Color(0xFFC6E6FF)
    );

    fun toIntArray(): IntArray {
        return intArrayOf(
            level0.toArgb(),
            level1.toArgb(),
            level2.toArgb(),
            level3.toArgb(),
            level4.toArgb()
        )
    }

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