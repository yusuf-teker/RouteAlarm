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
import org.yusufteker.routealarm.feature.alarm.data.repository.InMemoryAlarmRepository
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeViewModel
import org.yusufteker.routealarm.feature.alarm.data.database.DatabaseFactory
import org.yusufteker.routealarm.feature.alarm.presentation.SharedAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addstops.StopPickerViewModel


expect val platformModule: Module

val sharedModule = module {

    single { PopupManager() }

    single {
        // engine platforma gore degisiyor bu yüzde
        // expect val platformModule: Module tanımlamlı ve tüm platformlar için implemente (actual) etmeliyiz
        HttpClientFactory.createClient(get())
    }

    single { // Create Database
        get<DatabaseFactory>().create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    singleOf(constructor = ::InMemoryAlarmRepository).bind<AlarmRepository>()

    single { get<AlarmDatabase>().alarmDao } // Create AlarmDataBase
    single { get<AlarmDatabase>().stopDao } // Create AlarmDataBase

    viewModel { HomeViewModel(get(), get()) }

    viewModel { AddAlarmViewModel(get ()) }

    viewModel { StopPickerViewModel() }

    viewModel { SharedAlarmViewModel() }
}