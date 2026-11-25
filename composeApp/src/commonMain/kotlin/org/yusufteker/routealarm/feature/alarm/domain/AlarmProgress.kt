package org.yusufteker.routealarm.feature.alarm.domain

import org.yusufteker.routealarm.feature.alarm.data.mappers.toLocation
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.calculateDistance

data class ActiveStopProgress(
    val activeStopIndex: Int,
    val progress: Float,
    val remainingDistanceMeters: Float
)

fun calculateActiveStopProgress(
    alarm: Alarm,
    startLocation: Location?,
    currentLocation: Location?
): ActiveStopProgress? {
    if (currentLocation == null) return null

    val sortedStops = alarm.stops.sortedBy { it.orderIndex }
    val nextStop = sortedStops.firstOrNull { !it.isPassed } ?: return null
    val nextIndex = sortedStops.indexOf(nextStop)

    val origin: Location = if (nextIndex == 0) {
        startLocation ?: currentLocation
    } else {
        sortedStops[nextIndex - 1].toLocation()
    }

    val totalDistance = calculateDistance(
        origin.lat,
        origin.lng,
        nextStop.latitude,
        nextStop.longitude
    ).toFloat()

    val remaining = calculateDistance(
        currentLocation.lat,
        currentLocation.lng,
        nextStop.latitude,
        nextStop.longitude
    ).toFloat()

    val progress = if (totalDistance > 0f) {
        (1f - (remaining / totalDistance)).coerceIn(0f, 1f)
    } else {
        1f
    }

    return ActiveStopProgress(
        activeStopIndex = nextIndex,
        progress = progress,
        remainingDistanceMeters = remaining
    )
}

