package org.yusufteker.routealarm.feature.alarm.domain

data class Alarm(
    val id: Int,
    val title: String,
    val stops: List<Stop>,
    val isActive: Boolean = false,
    val passedStops: Int = 0
)
