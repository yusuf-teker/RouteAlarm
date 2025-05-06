package org.yusufteker.routealarm.feature.alarm.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.yusufteker.routealarm.core.domain.DataError
import org.yusufteker.routealarm.core.domain.Result
import org.yusufteker.routealarm.feature.alarm.data.database.AlarmDao
import org.yusufteker.routealarm.feature.alarm.data.mappers.toAlarm
import org.yusufteker.routealarm.feature.alarm.data.mappers.toAlarmEntity
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository

class InMemoryAlarmRepository(
    private val localAlarmDataSource: AlarmDao

) : AlarmRepository {

    override fun getAlarms(): Flow<List<Alarm>> {
         return localAlarmDataSource.getAlarms()
            .map {
                it.map { alarm -> alarm.toAlarm() }
            }

    }

    override suspend fun addAlarm(alarm: Alarm) {
        localAlarmDataSource.upsert(alarm.toAlarmEntity())

    }

    override suspend fun deleteAlarm(id: Int){
        localAlarmDataSource.deleteAlarmById(id)
    }

    override suspend fun getAlarmById(id: Int): Result<Alarm?, DataError.Remote> {
        val alarm = localAlarmDataSource.getAlarmById(id)
        return Result.Success(alarm?.toAlarm())

    }
}