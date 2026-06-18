package cn.waijade.nexuswid.di

import android.content.Context
import android.os.Build
import cn.waijade.nexuswid.data.StateRepository
import cn.waijade.nexuswid.data.WidgetPreferences
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.ui.settingsScreen.viewModel.SettingsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import org.koin.core.module.dsl.viewModel

val appModule = module {
    single<AppInfo> { create(::createAppInfo) }
    single<StateRepository> { StateRepository() }
    single<GitHubPreferences> { GitHubPreferences(get()) }
    single<WidgetPreferences> { get<GitHubPreferences>() }
    single<Json> { Json { ignoreUnknownKeys = true } }
    single<HttpClient> {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(get<Json>())
            }
        }
    }
    single<GitHubApiService> { GitHubApiService(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}

private fun createAppInfo(context: Context): AppInfo {
    val debug = context.packageName.endsWith(".debug")

    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "-"
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        0L
    }

    return AppInfo(debug, versionName, versionCode)
}
