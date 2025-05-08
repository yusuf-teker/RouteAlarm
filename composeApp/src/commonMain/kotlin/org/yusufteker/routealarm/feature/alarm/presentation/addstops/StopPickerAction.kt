package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.location.domain.Location

sealed interface StopPickerAction {
    data class QueryChanged(val value: String) : StopPickerAction
    data class LocationSelected(val location: Location) : StopPickerAction
    data class AddStop(val stop: Stop) : StopPickerAction
}
