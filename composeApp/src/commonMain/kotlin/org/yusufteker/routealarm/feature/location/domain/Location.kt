package org.yusufteker.routealarm.feature.location.domain

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
