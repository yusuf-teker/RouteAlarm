package org.yusufteker.routealarm.feature.location.domain

import org.yusufteker.routealarm.feature.location.data.IosLocationTrackingService

actual class LocationTracker {

    private val locationService = IosLocationTrackingService()

    actual fun startTracking(alarmId: Int) {
        locationService.startLocationUpdates(alarmId) { stop, isLastStop ->
            println("Stop reached (alarmId: $alarmId, isLast: $isLastStop): ${stop.name}")
        }
    }

    actual fun stopTracking() {
        locationService.stopLocationUpdates()
    }
}
