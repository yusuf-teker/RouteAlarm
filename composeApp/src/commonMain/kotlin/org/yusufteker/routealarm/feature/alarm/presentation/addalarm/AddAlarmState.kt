package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import org.yusufteker.routealarm.feature.alarm.domain.Stop

data class AddAlarmState(
    val title: String = "",
    val stops: List<Stop> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val canNavigateStopPicker: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)