package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.domain.TransportType
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.Place

data class StopPickerState(
    val query: String = "",
    val stop: Stop = Stop(name = "", latitude = 0.0, longitude = 0.0, alarmId = -1),
    val location: Location = Location("Necip Fazıl",41.014979, 29.180757),
    val selectedTransportType: TransportType = TransportType.BUS,
    val canAddAndNavigate: Boolean = false, // todo kötü yontem
    val suggestions: List<Place> = emptyList(),
    val selectedPlace: Place? = null

    )
