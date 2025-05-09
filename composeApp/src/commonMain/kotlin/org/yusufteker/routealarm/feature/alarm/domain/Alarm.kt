package org.yusufteker.routealarm.feature.alarm.domain

data class Alarm(
    val id: Int = 0,
    val title: String,
    val isActive: Boolean,
    val timeInMillis: Long = -1,
    val soundUri: String = "",
    val isVibration: Boolean = true,
    val stops: List<Stop> = emptyList()
)