package org.yusufteker.routealarm.feature.location.domain


expect class LocationTracker {
    fun startTracking(alarmId: Int)
    fun stopTracking()
}