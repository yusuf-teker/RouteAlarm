@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.RoomDatabaseConstructor


//Room arka planda oluşturuyor implementation
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AlarmDatabaseConstructor: RoomDatabaseConstructor<AlarmDatabase> {
    override fun initialize(): AlarmDatabase
}