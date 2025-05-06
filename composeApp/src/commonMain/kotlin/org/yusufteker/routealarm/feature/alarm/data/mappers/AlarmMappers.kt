package org.yusufteker.routealarm.feature.alarm.data.mappers

import org.yusufteker.routealarm.feature.alarm.data.database.AlarmEntity
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.Stop

//TODO ENTITY STOP ILISKISI KURULACAK SONRA
fun AlarmEntity.toAlarm(): Alarm {
    return Alarm(
        id = id,
        title = title,
        stops = listOf(
            Stop(
                id = 0,
                name = locationName,
                latitude = latitude,
                longitude = longitude,
                isPassed = false
            )
        ),
        isActive = isActive,
        passedStops = 0
    )
}

fun Alarm.toAlarmEntity(): AlarmEntity {
    val firstStop = stops.firstOrNull() ?: Stop(0,"", 0.0, 0.0)
    return AlarmEntity(
        id = id,
        title = title,
        locationName = firstStop.name,
        latitude = firstStop.latitude,
        longitude = firstStop.longitude,
        isActive = isActive,
        timeInMillis = 0L
    )
}
