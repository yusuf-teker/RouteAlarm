package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.domain.TransportType
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.Place

sealed interface StopPickerAction {
    data class QueryChanged(val value: String) : StopPickerAction
    data class LocationSelected(val location: Location) : StopPickerAction
    data class AddStop(val stop: Stop) : StopPickerAction
    data class SelectTransportType(val type: TransportType) : StopPickerAction
    data class TitleChanged(val newTitle: String) : StopPickerAction
    data class SuggestionSelected(val suggestionPlace: Place) : StopPickerAction

    object NavigateBack : StopPickerAction
}
