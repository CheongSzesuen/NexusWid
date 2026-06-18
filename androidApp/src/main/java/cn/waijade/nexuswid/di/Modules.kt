package cn.waijade.nexuswid.di

import android.content.Context
import android.os.Build
import cn.waijade.nexuswid.data.StateRepository
import cn.waijade.nexuswid.ui.settingsScreen.viewModel.SettingsViewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import org.koin.core.module.dsl.viewModel

val appModule = module {
    single<AppInfo> { create(::createAppInfo) }
    single<StateRepository> { StateRepository() }
    viewModel { SettingsViewModel(get()) }
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
