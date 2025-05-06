package org.yusufteker.routealarm.feature.alarm.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(
    private val context: Context
) {
    actual fun create(): RoomDatabase.Builder<AlarmDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(AlarmDatabase.DATABASE_NAME)

        return Room.databaseBuilder(
            context,
            dbFile.absolutePath)
    }
}