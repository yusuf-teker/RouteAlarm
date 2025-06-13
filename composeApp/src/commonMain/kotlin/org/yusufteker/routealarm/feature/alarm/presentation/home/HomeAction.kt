package org.yusufteker.routealarm.feature.alarm.presentation.home

import org.yusufteker.routealarm.feature.alarm.domain.Alarm

sealed interface HomeAction {
    data class OnAlarmCheckedChange(val alarm: Alarm, val isChecked: Boolean) : HomeAction
    data class OnAlarmClick(val alarmId: Int) : HomeAction
    data class OnDeleteAlarm(val alarmId: Int) : HomeAction

    object OnAddAlarmClick : HomeAction

}