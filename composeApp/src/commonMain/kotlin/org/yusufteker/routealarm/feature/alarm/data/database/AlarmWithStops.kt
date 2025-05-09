package org.yusufteker.routealarm.feature.alarm.data.database

import androidx.room.Embedded
import androidx.room.Relation
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopEntity

data class AlarmWithStops(
    @Embedded val alarm: AlarmEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "alarmId"
    )
    val stops: List<StopEntity>
)