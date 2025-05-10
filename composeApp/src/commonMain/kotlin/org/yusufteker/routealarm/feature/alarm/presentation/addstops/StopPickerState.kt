package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.domain.TransportType

data class StopPickerState(
    val query: String = "",
    val stop: Stop = Stop(name = "", latitude = 0.0, longitude = 0.0, alarmId = -1),
    val selectedTransportType: TransportType = TransportType.BUS,
    val canAddAndNavigate: Boolean = false // todo kötü yontem
)
