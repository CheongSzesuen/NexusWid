package cn.waijade.nexuswid.widget

import android.content.Context
import android.util.Log
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "HeatmapWidgetDataStore"

class HeatmapWidgetDataStore(context: Context) {
    private val githubPreferences = GitHubPreferences(context.applicationContext)
    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            header("Accept-Language", "en-US,en;q=0.5")
        }
    }
    private val githubApiService = GitHubApiService(httpClient, json)

    fun getGrid(columns: Int, rows: Int = HeatmapGridCalculator.ROWS, weekStartsOnMonday: Boolean = false): List<Int> {
        val levelsByDate = getContributionLevels()
        return buildGridFromLevels(levelsByDate, columns, rows, weekStartsOnMonday)
    }

    fun getContributionLevels(): Map<String, Int> {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val username = githubPreferences.username
                val token = githubPreferences.token.takeIf { it.isNotBlank() }

                Log.d(TAG, "getContributionLevels: username=$username, token=${if (token.isNullOrBlank()) "empty" else "set"}")

                if (username.isBlank()) {
                    Log.d(TAG, "getContributionLevels: username is blank, returning empty map")
                    return@runCatching emptyMap()
                }

                val contributionDays = githubApiService.getContributionData(
                    username = username,
                    token = token
                ).getOrThrow()

                Log.d(TAG, "getContributionLevels: got ${contributionDays.size} contribution days")

                val levelsByDate = mutableMapOf<String, Int>()
                contributionDays.forEach { day ->
                    val level = day.color.toIntOrNull() ?: 0
                    levelsByDate[day.date] = level
                }

                Log.d(TAG, "getContributionLevels: levelsByDate size=${levelsByDate.size}")

                levelsByDate
            }.getOrElse {
                Log.e(TAG, "getContributionLevels: error", it)
                emptyMap()
            }
        }
    }

    fun buildGridFromLevels(
        levelsByDate: Map<String, Int>,
        columns: Int,
        rows: Int,
        weekStartsOnMonday: Boolean
    ): List<Int> {
        val today = LocalDate.now()
        val todayDow = today.dayOfWeek.value // 1=周一, 7=周日

        val offsetFromStart = if (weekStartsOnMonday) {
            todayDow - 1        // 周一=0, 周日=6
        } else {
            todayDow % 7        // 周日=0, 周六=6
        }
        val lastColumnStart = today.minusDays(offsetFromStart.toLong())
        val startDate = lastColumnStart.minusDays(((columns - 1) * 7L))

        val grid = mutableListOf<Int>()
        for (col in 0 until columns) {
            for (row in 0 until rows) {
                val date = startDate.plusDays((col * 7 + row).toLong())
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                grid.add(levelsByDate[dateStr] ?: 0)
            }
        }
        return grid
    }
}