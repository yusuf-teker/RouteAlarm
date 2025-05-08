package org.yusufteker.routealarm.feature.location.domain

data class Location(
    val id: String = "",
    val name: String = "",
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)
