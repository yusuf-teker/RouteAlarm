package org.yusufteker.routealarm.feature.alarm.data.database.stop

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.yusufteker.routealarm.feature.alarm.data.database.AlarmEntity

// Stop.kt
@Entity(
    tableName = "stops",
    foreignKeys = [ForeignKey(
        entity = AlarmEntity::class,
        parentColumns = ["id"],
        childColumns = ["alarmId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class StopEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val alarmId: Int, // Hangi alarm'a ait olduğu
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val orderIndex: Int = -1,
    val radius: Int = 100,
    val isPassed: Boolean = false

)