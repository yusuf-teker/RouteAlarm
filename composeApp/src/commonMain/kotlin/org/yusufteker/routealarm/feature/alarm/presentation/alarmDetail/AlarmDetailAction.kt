package org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail

import org.yusufteker.routealarm.feature.alarm.domain.Alarm

sealed class AlarmDetailAction {
    data class LoadData(val alarmId: Int) : AlarmDetailAction()
    data class OnAlarmCheckedChange(val alarm: Alarm, val isChecked: Boolean) : AlarmDetailAction()

    data class OnDeleteAlarm(val alarmId: Int) : AlarmDetailAction()
    object NavigateBack : AlarmDetailAction()


}