package org.yusufteker.routealarm.feature.alarm.data.database.stop

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StopDao {
    @Query("SELECT * FROM stops WHERE alarmId = :alarmId ORDER BY orderIndex ASC")
    suspend fun getStopsForAlarm(alarmId: Long): List<StopEntity>
    // Yeni stop ekleme (ID döner)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stop: StopEntity): Long

    // Toplu stop ekleme
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stops: List<StopEntity>)

    // Alarm ID'sine göre stopları getirme
    @Query("SELECT * FROM stops WHERE alarmId = :alarmId ORDER BY orderIndex ASC")
    fun getStopsByAlarmId(alarmId: Long): Flow<List<StopEntity>>

    // Tek stop silme
    @Delete
    suspend fun delete(stop: StopEntity)

    // Alarm'a ait tüm stopları silme
    @Query("DELETE FROM stops WHERE alarmId = :alarmId")
    suspend fun deleteAllStopsForAlarm(alarmId: Long)

    // Stop güncelleme
    @Update
    suspend fun update(stop: StopEntity)

    // Koordinata yakın stopları bulma
    @Query("""
        SELECT * FROM stops 
        WHERE latitude BETWEEN :minLat AND :maxLat 
        AND longitude BETWEEN :minLon AND :maxLon
        AND alarmId = :alarmId
    """)
    suspend fun getStopsInArea(
        alarmId: Long,
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<StopEntity>

    // Belirli bir yarıçap içindeki stoplar
    @Query("""
        SELECT * FROM stops 
        WHERE alarmId = :alarmId 
        AND isPassed = 0 
        ORDER BY (
            (:lat - latitude) * (:lat - latitude) + 
            (:lon - longitude) * (:lon - longitude)
        ) ASC 
        LIMIT 1
    """)
    suspend fun getNearestStop(
        alarmId: Long,
        lat: Double,
        lon: Double
    ): StopEntity?
}