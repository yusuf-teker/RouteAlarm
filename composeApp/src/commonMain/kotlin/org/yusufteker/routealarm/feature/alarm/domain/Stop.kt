package org.yusufteker.routealarm.feature.alarm.domain

data class Stop(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isPassed: Boolean = false
)
