package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.RoomDatabaseConstructor


//Room arka planda oluşturuyor implementation
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AlarmDatabaseConstructor: RoomDatabaseConstructor<AlarmDatabase> {
    override fun initialize(): AlarmDatabase
}