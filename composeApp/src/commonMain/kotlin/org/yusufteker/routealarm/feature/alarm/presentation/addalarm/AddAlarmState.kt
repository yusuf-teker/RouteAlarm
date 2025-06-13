package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import org.jetbrains.compose.resources.StringResource
import org.yusufteker.routealarm.feature.alarm.domain.Stop

data class AddAlarmState(
    val title: String = "",
    val stops: List<Stop> = emptyList(),
    val isLocationPermissionGranted: Boolean = false,
    val isLoading: Boolean = false,
    val error: StringResource? = null
)