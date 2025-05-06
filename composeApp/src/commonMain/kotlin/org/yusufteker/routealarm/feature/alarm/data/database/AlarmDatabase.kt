package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AlarmEntity::class],
    version = 1,
)

//@TypeConverters(Converters::class)
@ConstructedBy(AlarmDatabaseConstructor::class)
abstract class AlarmDatabase: RoomDatabase() {
    abstract val alarmDao: AlarmDao

    companion object {
        const val DATABASE_NAME = "alarms_db"
    }
}
