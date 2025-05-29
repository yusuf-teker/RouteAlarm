package org.yusufteker.routealarm.feature.location.domain


import org.yusufteker.routealarm.feature.location.data.IosLocationTrackingService

actual class LocationTracker {

    private val locationService = IosLocationTrackingService()



    actual fun startTracking(alarmId: Int) {
        locationService.startLocationUpdates(alarmId) { stop, isLastStop ->
            println("Konum geldi (alarmId: $alarmId): ${stop.latitude}, ${stop.longitude}")

            if (isLastStop){
                println("Last Stop Reached")
                stopTracking()
            }
        }
    }

    actual fun stopTracking() {
        locationService.stopLocationUpdates()
    }
}