package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import org.yusufteker.routealarm.feature.alarm.domain.Stop

sealed class AddAlarmAction {
    data class TitleChanged(val newTitle: String) : AddAlarmAction()
    data class RemoveStop(val stop: Stop) : AddAlarmAction()
    object SaveAlarm : AddAlarmAction()
    data class OnStopsChange(val stops: List<Stop>) : AddAlarmAction()
    object RequestLocationPermission: AddAlarmAction()
    object CheckLocationPermission: AddAlarmAction()
}