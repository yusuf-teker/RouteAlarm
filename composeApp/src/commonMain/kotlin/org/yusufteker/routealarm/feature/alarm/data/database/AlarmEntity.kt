package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isActive: Boolean,
    val timeInMillis: Long,
    val soundUri: String = "",
    val isVibration: Boolean = true

)