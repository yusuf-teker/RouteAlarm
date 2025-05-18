package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopEntity

@Dao
interface AlarmDao {


    @Query("SELECT * FROM alarms")
    fun getAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alarm: AlarmEntity)

    @Insert
    suspend fun insert(alarm: AlarmEntity): Long

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<AlarmEntity>)


    @Transaction
    @Query("SELECT * FROM alarms")
    fun getAlarmsWithStops(): Flow<List<AlarmWithStops>>

    @Transaction
    @Query("SELECT * FROM alarms WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAlarmWithStops(): AlarmWithStops?

    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :alarmId")
    suspend fun updateIsActive(alarmId: Int, isActive: Boolean)

    @Query("SELECT * FROM stops WHERE alarmId = :alarmId")
    suspend fun getStopsForAlarm(alarmId: Int): List<StopEntity>


}