package org.yusufteker.routealarm.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import org.yusufteker.routealarm.feature.alarm.data.database.DatabaseFactory
import org.yusufteker.routealarm.feature.location.data.LocationService
import org.yusufteker.routealarm.feature.location.data.LocationTrackingService
import org.yusufteker.routealarm.feature.location.data.PlaceSuggestionService
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer
import org.yusufteker.routealarm.feature.location.domain.LocationTracker
import org.yusufteker.routealarm.notification.NotificationManager
import org.yusufteker.routealarm.settings.SettingsManager
import org.yusufteker.routealarm.settings.createDataStore

actual val platformModule: Module
    get() = module {
        single<HttpClientEngine> { OkHttp.create() } // Platform spesifik dependency
        single { DatabaseFactory(androidApplication()) }
        single { PlaceSuggestionService(get()) }
        single { LocationService(get()) }
        single { LocationTracker(get(), get()) }
        single { AlarmSoundPlayer(get()) }
        single { NotificationManager(get()) }
        single<DataStore<Preferences>> {
            createDataStore(androidApplication())
        }
        single { SettingsManager(get()) }
        single { LocationTrackingService() }
    }