package org.yusufteker.routealarm.feature.location.domain

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.*

data class Location(
    val name: String,
    val lat: Double,
    val lng: Double
)
val emptyLocation = Location(
    name = "",
    lat = 0.0,
    lng = 0.0
)

fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val R = 6371000.0 // Earth radius in meters
    val dLat = (lat2 - lat1)
    val dLon = (lon2 - lon1).toRadians()
    val a = sin(dLat / 2).pow(2.0) + cos(lat1.toRadians()) *
            cos(lat2.toRadians()) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}
fun Double.toRadians(): Double = this * PI / 180

val STOP_PROXIMITY_THRESHOLD_METERS = 250