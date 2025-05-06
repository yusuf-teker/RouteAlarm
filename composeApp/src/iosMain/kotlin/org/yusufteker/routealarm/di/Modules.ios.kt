package org.yusufteker.routealarm.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.yusufteker.routealarm.feature.alarm.data.database.DatabaseFactory

actual val platformModule: Module
    get() = module {
        single<HttpClientEngine> { Darwin.create() } // Platform spesifik dependency
       single { DatabaseFactory() }
    }