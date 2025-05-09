package org.yusufteker.routealarm.feature.alarm.domain

data class Stop(
    val id: Int = 0,
    val alarmId: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val orderIndex: Int = -1,
    val radius: Int = 100,
    val isPassed: Boolean = false
)
