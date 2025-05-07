package org.yusufteker.routealarm.feature.alarm.domain

import kotlinx.coroutines.flow.Flow
import org.yusufteker.routealarm.core.domain.DataError
import org.yusufteker.routealarm.core.domain.Result

interface AlarmRepository {
    fun getAlarms(): Flow<List<Alarm>>
    suspend fun addAlarm(alarm: Alarm)
    suspend fun deleteAlarm(id: Int)
    suspend fun getAlarmById(id: Int): Result<Alarm?, DataError>
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun insertAlarms(alarms: List<Alarm>)
    suspend fun isAnyActiveAlarm(): Boolean
}