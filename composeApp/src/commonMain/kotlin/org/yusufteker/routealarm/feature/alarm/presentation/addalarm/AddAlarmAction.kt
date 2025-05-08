package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import org.yusufteker.routealarm.feature.alarm.domain.Stop

sealed class AddAlarmAction {
    data class TitleChanged(val newTitle: String) : AddAlarmAction()
    object AddStop : AddAlarmAction()
    data class RemoveStop(val stop: Stop) : AddAlarmAction()
    object SaveAlarm : AddAlarmAction()
    object ClearError : AddAlarmAction()
    data class OnStopsChange(val stops: List<Stop>) : AddAlarmAction()
}