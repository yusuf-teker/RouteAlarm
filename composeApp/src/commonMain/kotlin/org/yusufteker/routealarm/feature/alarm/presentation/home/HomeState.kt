package org.yusufteker.routealarm.feature.alarm.presentation.home

import org.yusufteker.routealarm.feature.alarm.domain.Alarm


data class HomeState(
    val alarms: List<Alarm> = emptyList(),
    val activeAlarm: Alarm? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
