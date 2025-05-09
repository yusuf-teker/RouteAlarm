package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopDao
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopEntity

@Database(
    entities = [AlarmEntity::class, StopEntity::class],
    version = 3,
    exportSchema = false
)

//@TypeConverters(Converters::class)
@ConstructedBy(AlarmDatabaseConstructor::class)
abstract class AlarmDatabase: RoomDatabase() {
    abstract val alarmDao: AlarmDao
    abstract val stopDao : StopDao

    companion object {
        const val DATABASE_NAME = "alarms_db"
    }
}
