package org.yusufteker.routealarm.feature.alarm.data.repository

import androidx.room.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.yusufteker.routealarm.core.domain.DataError
import org.yusufteker.routealarm.core.domain.Result
import org.yusufteker.routealarm.feature.alarm.data.database.AlarmDao
import org.yusufteker.routealarm.feature.alarm.data.database.AlarmWithStops
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopDao
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopEntity
import org.yusufteker.routealarm.feature.alarm.data.mappers.toDomain
import org.yusufteker.routealarm.feature.alarm.data.mappers.toEntity
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import kotlin.collections.map

class InMemoryAlarmRepository(
    private val localAlarmDataSource: AlarmDao,
    private val stopDao: StopDao,
) : AlarmRepository {


    @Transaction
    override suspend fun saveAlarmWithStops(alarm: Alarm, stops: List<Stop>): Int {

        // 1. Alarmı ekle ve ID'yi al
        val alarmId = localAlarmDataSource.insert(alarm.toEntity())

        // 2. Stop'ları bu ID ile ekle
        val stopEntities = stops.map { stop ->
            stop.toEntity().copy(alarmId = alarmId.toInt())
        }
        stopDao.insertAll(stopEntities)

        return alarmId.toInt()

    }


    override fun getAlarms(): Flow<List<Alarm>> {
        return localAlarmDataSource.getAlarms().map {
                it.map { alarm -> alarm.toDomain() }
            }

    }

    override suspend fun addAlarm(alarm: Alarm) {
        localAlarmDataSource.upsert(alarm.toEntity())

    }

    override suspend fun deleteAlarm(id: Int) {
        localAlarmDataSource.deleteAlarmById(id)
    }

    override suspend fun getAlarmById(id: Int): Result<Alarm?, DataError.Remote> {
        val alarm = localAlarmDataSource.getAlarmById(id)
        return Result.Success(alarm?.toDomain())

    }

    override suspend fun updateAlarm(alarm: Alarm) {
        localAlarmDataSource.upsert(alarm.toEntity())
    }

    override suspend fun insertAlarms(alarms: List<Alarm>) {
        localAlarmDataSource.insertAlarms(alarms.map { it.toEntity() })
    }

    override suspend fun getActiveAlarmWithStops(): AlarmWithStops? {
        return localAlarmDataSource.getActiveAlarmWithStops()
    }

    override suspend fun getAlarmsWithStops(): Flow<List<Alarm>> {
        return localAlarmDataSource.getAlarmsWithStops().map { list -> list.map { it.toDomain() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAlarmsWithStops2(): Flow<List<Alarm>> {
        return localAlarmDataSource.getAlarms().flatMapLatest { alarmEntities ->
            if (alarmEntities.isEmpty()) {
                flowOf(emptyList())
            } else {
                val alarmFlows = alarmEntities.map { alarmEntity ->
                    stopDao.getStopsByAlarmId(alarmEntity.id).map { stopEntities ->
                        val stops = stopEntities.map { it.toDomain() }
                        alarmEntity.toDomain(stops)
                    }
                }
                combine(alarmFlows) { it.toList() } // Her alarm + durakları eşlenmiş olur
            }
        }
    }

    override suspend fun insertStop(stop: StopEntity) {
        stopDao.insert(stop)
    }

    override suspend fun setAlarmActive(alarmId: Int, isActive: Boolean) {
        localAlarmDataSource.updateIsActive(alarmId, isActive)
    }

    override suspend fun getAlarmByIdWithStops(id: Int): Alarm? {
        val alarm = localAlarmDataSource.getAlarmById(id)
        val stops: List<StopEntity> = localAlarmDataSource.getStopsForAlarm(id)
        return if (alarm != null) {
            Alarm(
                id = alarm.id,
                title = alarm.title,
                isActive = alarm.isActive,
                timeInMillis = alarm.timeInMillis,
                soundUri = alarm.soundUri,
                isVibration = alarm.isVibration,
                stops = stops.map {
                    Stop(
                        id = it.id,
                        alarmId = it.alarmId,
                        name = it.name,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        isPassed = it.isPassed
                    )
                })
        } else {
            null
        }
    }

    override fun getAlarmWithStopsByIdFlow(id: Int): Flow<Alarm?> {
        return localAlarmDataSource.getAlarmWithStopsByIdFlow(id).map {  it?.toDomain()  }
    }


    override suspend fun setStopIsPassed(stopId: Int, isPassed: Boolean) {
        println("setStopIsPassed called for stopId: $stopId $isPassed")
        localAlarmDataSource.setStopIsPassed(stopId, isPassed)
    }

    override suspend fun setAllStopIsPassed(isPassed: Boolean) {
        localAlarmDataSource.setAllStopIsPassed(false)
    }


    override suspend fun triggerAlarmUpdate(alarmId: Int) {
        localAlarmDataSource.triggerAlarmUpdate(alarmId)
    }

}