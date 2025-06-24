package org.yusufteker.routealarm.feature.location.domain

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)
fun Place.toLocation(): Location {
    return Location(
        name = this.name,
        lat = this.latitude,
        lng = this.longitude
    )
}