package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.RoomDatabase

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<AlarmDatabase>
}