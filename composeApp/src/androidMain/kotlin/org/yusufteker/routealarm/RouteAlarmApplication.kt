    package org.yusufteker.routealarm

    import android.app.Application
    import android.content.Context
    import com.google.android.libraries.places.api.Places
    import io.github.aakira.napier.DebugAntilog
    import io.github.aakira.napier.Napier
    import org.koin.android.ext.koin.androidContext
    import org.yusufteker.routealarm.di.initKoin

    class RouteAlarmApplication: Application() {
        override fun onCreate() {
            super.onCreate()
            Napier.base(DebugAntilog()) // init logger
            initKoin {
                androidContext(this@RouteAlarmApplication)
            }
            AndroidApplicationContext.init(this)
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
            }
        }
    }

    object AndroidApplicationContext {
        lateinit var appContext: Context
            private set

        fun init(context: Context) {
            appContext = context
        }
    }