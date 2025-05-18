package org.yusufteker.routealarm.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import org.yusufteker.routealarm.feature.alarm.data.database.DatabaseFactory
import org.yusufteker.routealarm.feature.location.data.LocationService
import org.yusufteker.routealarm.feature.location.data.PlaceSuggestionService
import org.yusufteker.routealarm.feature.location.domain.LocationTracker

actual val platformModule: Module
    get() = module {
        single<HttpClientEngine> { OkHttp.create() } // Platform spesifik dependency
        single { DatabaseFactory(androidApplication()) }
        single { PlaceSuggestionService(get()) }
        single { LocationService(get()) }
        single { LocationTracker(get(), get()) }
    }