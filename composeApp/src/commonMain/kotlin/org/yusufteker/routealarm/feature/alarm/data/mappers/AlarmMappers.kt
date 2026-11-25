package org.yusufteker.routealarm.feature.alarm.data.mappers

import org.yusufteker.routealarm.feature.alarm.data.database.AlarmEntity
import org.yusufteker.routealarm.feature.alarm.data.database.AlarmWithStops
import org.yusufteker.routealarm.feature.alarm.data.database.stop.StopEntity
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.domain.TransportType
import org.yusufteker.routealarm.feature.location.domain.Location

fun AlarmEntity.toDomain(stops: List<Stop> = emptyList()): Alarm {
    return Alarm(
        id = this.id,
        title = this.title,
        isActive = this.isActive,
        timeInMillis = this.timeInMillis,
        soundUri = this.soundUri,
        isVibration = this.isVibration,
        stops = stops.sortedBy { it.orderIndex }
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

fun StopEntity.toDomain(): Stop {
    val parsedTransport = TransportType.entries.find { it.name == this.transportType }
        ?: TransportType.DEFAULT
    return Stop(
        id = this.id,
        alarmId = this.alarmId,
        isPassed = this.isPassed,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address,
        orderIndex = this.orderIndex,
        radius = this.radius,
        transportType = parsedTransport
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
        radius = this.radius,
        isPassed = this.isPassed,
        transportType = this.transportType.name
    )
}

fun Stop.toLocation(): Location {
    return Location(
        name = this.name,
        lat = this.latitude,
        lng = this.longitude
    )
}

fun AlarmWithStops.toDomain(): Alarm {
    return alarm.toDomain(stops = stops.map { it.toDomain() })
}
