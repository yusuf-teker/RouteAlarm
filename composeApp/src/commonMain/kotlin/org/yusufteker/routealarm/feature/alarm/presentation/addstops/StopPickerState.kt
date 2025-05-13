package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.domain.TransportType
import org.yusufteker.routealarm.feature.location.domain.LocationUiModel

data class StopPickerState(
    val query: String = "",
    val stop: Stop = Stop(name = "", latitude = 0.0, longitude = 0.0, alarmId = -1),
    val locationUiModel: LocationUiModel = LocationUiModel("",-1.toDouble(),-1.toDouble()),
    val selectedTransportType: TransportType = TransportType.BUS,
    val canAddAndNavigate: Boolean = false, // todo kötü yontem
)
