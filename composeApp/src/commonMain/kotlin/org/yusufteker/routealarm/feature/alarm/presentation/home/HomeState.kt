package org.yusufteker.routealarm.feature.alarm.presentation.home

import org.yusufteker.routealarm.feature.alarm.domain.Alarm

data class HomeState(
    val alarms: List<Alarm> = emptyList(),
    val activeAlarm: Alarm? = null,
    val activeStopIndex: Int = -1,
    val activeStopProgress: Float = 0f,
    val remainingDistanceMeters: Double? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
