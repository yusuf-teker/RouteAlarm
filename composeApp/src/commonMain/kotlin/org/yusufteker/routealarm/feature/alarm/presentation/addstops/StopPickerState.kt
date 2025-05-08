package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import org.yusufteker.routealarm.feature.alarm.domain.Stop

data class StopPickerState(
    val query: String = "",
    val stop: Stop? = null
)
