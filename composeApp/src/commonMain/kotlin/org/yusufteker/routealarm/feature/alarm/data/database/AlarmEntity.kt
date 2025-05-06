package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val isActive: Boolean,
    val timeInMillis: Long
)