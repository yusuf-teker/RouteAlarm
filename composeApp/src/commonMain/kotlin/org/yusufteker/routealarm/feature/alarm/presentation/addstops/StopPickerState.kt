package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.domain.TransportType
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.Place
import org.yusufteker.routealarm.feature.location.domain.emptyLocation

data class StopPickerState(
    val query: String = "",
    val stop: Stop = Stop(name = "", latitude = 0.0, longitude = 0.0, alarmId = -1),
    val location: Location = emptyLocation,
    val selectedTransportType: TransportType = TransportType.BUS,
    val suggestions: List<Place> = emptyList(),
    val selectedPlace: Place? = null,
    val currentLocation: Location = emptyLocation,
    val centerToCurrentLocation: Boolean = false,
    val showBottom: Boolean = false,
    )
