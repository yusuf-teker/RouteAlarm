package org.yusufteker.routealarm.feature.alarm.data.mappers

import org.yusufteker.routealarm.feature.alarm.data.database.AlarmEntity
import org.yusufteker.routealarm.feature.alarm.data.database.AlarmWithStops
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopEntity
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.Stop

fun AlarmEntity.toDomain(stops: List<Stop> = emptyList()): Alarm {
    return Alarm(
        id = this.id,
        title = this.title,
        isActive = this.isActive,
        timeInMillis = this.timeInMillis,
        soundUri = this.soundUri,
        isVibration = this.isVibration,
        stops = stops
    )
}

fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = this.id,
        title = this.title,
        isActive = this.isActive,
        timeInMillis = this.timeInMillis,
        soundUri = this.soundUri,
        isVibration = this.isVibration
    )
}

// feature/alarm/data/mapper/StopMapper.kt
fun StopEntity.toDomain(): Stop {
    return Stop(
        id = this.id,
        alarmId = this.alarmId,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address,
        orderIndex = this.orderIndex,
        radius = this.radius
    )
}

fun Stop.toEntity(): StopEntity {
    return StopEntity(
        id = this.id,
        alarmId = this.alarmId,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address,
        orderIndex = this.orderIndex,
        radius = this.radius
    )
}

fun AlarmWithStops.toDomain(): Alarm {
    return Alarm(
        id = alarm.id,
        title = alarm.title,
        isActive = alarm.isActive,
        stops = stops.map { it.toDomain() }
    )
}
