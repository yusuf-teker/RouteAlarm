package org.yusufteker.routealarm.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.yusufteker.routealarm.core.data.HttpClientFactory
import org.yusufteker.routealarm.core.presentation.popup.PopupManager
import org.yusufteker.routealarm.feature.alarm.data.database.AlarmDatabase
import org.yusufteker.routealarm.feature.alarm.data.database.DatabaseFactory
import org.yusufteker.routealarm.feature.alarm.data.repository.InMemoryAlarmRepository
import org.yusufteker.routealarm.feature.alarm.domain.AlarmActivationHandler
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.alarm.presentation.SharedAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addstops.StopPickerViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail.AlarmDetailViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeViewModel
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.settings.SettingsManager
import org.yusufteker.routealarm.settings.SettingsViewModel

expect val platformModule: Module

val sharedModule = module {

    single { PopupManager() }
    single { PermissionBridge() }
    single {
        HttpClientFactory.createClient(get())
    }

    single {
        get<DatabaseFactory>().create()
            .setDriver(BundledSQLiteDriver())
            .addMigrations(AlarmDatabase.MIGRATION_4_5)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    singleOf(constructor = ::InMemoryAlarmRepository).bind<AlarmRepository>()

    single { get<AlarmDatabase>().alarmDao }
    single { get<AlarmDatabase>().stopDao }

    single { SettingsManager(get()) }
    single { AlarmActivationHandler(get(), get(), get(), get(), get()) }

    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { AddAlarmViewModel(get(), get()) }
    viewModel { StopPickerViewModel(get(), get()) }
    viewModel { SharedAlarmViewModel() }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { AlarmDetailViewModel(get(), get(), get(), get(), get(), get()) }
}
