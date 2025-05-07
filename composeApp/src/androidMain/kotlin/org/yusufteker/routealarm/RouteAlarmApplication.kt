package org.yusufteker.routealarm

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.yusufteker.routealarm.di.initKoin

class RouteAlarmApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@RouteAlarmApplication)
        }
    }
}