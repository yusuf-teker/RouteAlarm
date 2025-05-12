package org.yusufteker.routealarm

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.yusufteker.routealarm.di.initKoin

class RouteAlarmApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@RouteAlarmApplication)
        }
        AndroidApplicationContext.init(this)

    }
}

object AndroidApplicationContext {
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context
    }
}