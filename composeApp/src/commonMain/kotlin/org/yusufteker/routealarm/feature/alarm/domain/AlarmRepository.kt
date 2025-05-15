package org.yusufteker.routealarm.feature.alarm.domain

import kotlinx.coroutines.flow.Flow
import org.yusufteker.routealarm.core.domain.DataError
import org.yusufteker.routealarm.core.domain.Result
import org.yusufteker.routealarm.feature.alarm.data.database.AlarmWithStops
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopEntity

interface AlarmRepository {
    fun getAlarms(): Flow<List<Alarm>>
    suspend fun addAlarm(alarm: Alarm)
    suspend fun deleteAlarm(id: Int)
    suspend fun getAlarmById(id: Int): Result<Alarm?, DataError>
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun insertAlarms(alarms: List<Alarm>)
    suspend fun getActiveAlarmWithStops(): AlarmWithStops?
    suspend fun saveAlarmWithStops(alarm: Alarm, stops: List<Stop>): Int
    suspend fun getAlarmsWithStops(): Flow<List<Alarm>>

    // Stop
    suspend fun insertStop(stop: StopEntity)

    suspend fun setAlarmActive(alarmId: Int, isActive: Boolean)


}