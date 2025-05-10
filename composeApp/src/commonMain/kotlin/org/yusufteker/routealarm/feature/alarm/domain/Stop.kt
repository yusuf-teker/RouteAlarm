package org.yusufteker.routealarm.feature.alarm.domain

import org.jetbrains.compose.resources.DrawableResource
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.bus
import routealarm.composeapp.generated.resources.default_transport
import routealarm.composeapp.generated.resources.metro
import routealarm.composeapp.generated.resources.metrobus
import routealarm.composeapp.generated.resources.minibus
import routealarm.composeapp.generated.resources.taxi
import routealarm.composeapp.generated.resources.train

enum class TransportType(val iconRes: DrawableResource) { // Int değil çünkü multiplatform
    BUS(Res.drawable.bus),
    METRO(Res.drawable.metro),
    MINIBUS(Res.drawable.minibus),
    METROBUS(Res.drawable.metrobus),
    TAXI(Res.drawable.taxi),
    TRAIN(Res.drawable.train),
    DEFAULT(Res.drawable.default_transport)
}

data class Stop(
    val id: Int = 0,
    val alarmId: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val orderIndex: Int = -1,
    val radius: Int = 100,
    val isPassed: Boolean = false,
    val transportType: TransportType = TransportType.DEFAULT
)
