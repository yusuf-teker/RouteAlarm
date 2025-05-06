package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.PrimaryKey

data class StopEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alarmId: Int, // foreign key
    val name: String,
    val latitude: Double,
    val longitude: Double
)