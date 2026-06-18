package cn.waijade.nexuswid

import android.app.Application
import cn.waijade.nexuswid.di.appModule
import org.koin.core.context.startKoin

class NexusWidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule)
        }
    }
}
