package org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail

import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.location.domain.Location

data class AlarmDetailState(
    val alarm: Alarm? = null,
    val alarms: List<Alarm> = emptyList(),
    val activeStop: Stop? = null,
    val startLocation: Location? = null,
    val currentDistanceToNextStop: Double? = null,
    val currentLocation: Location? = null,
    val progress: Float = 0f,
    val isAlarmActive: Boolean = false,
    val isLoading: Boolean = false,
    val remainingDistance: Float = 0f,
    val previousStops: List<Stop> = emptyList(),
    val nextStops: List<Stop> = emptyList()
)
