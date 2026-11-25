package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopDao
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopEntity

@Database(
    entities = [AlarmEntity::class, StopEntity::class],
    version = 5,
    exportSchema = false
)
@ConstructedBy(AlarmDatabaseConstructor::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract val alarmDao: AlarmDao
    abstract val stopDao: StopDao

    companion object {
        const val DATABASE_NAME = "alarms_db"

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE stops ADD COLUMN transportType TEXT NOT NULL DEFAULT 'DEFAULT'"
                )
            }
        }
    }
}
