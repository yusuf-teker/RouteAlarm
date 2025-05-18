package org.yusufteker.routealarm.feature.alarm.domain

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.yusufteker.routealarm.feature.location.domain.Place
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.bus
import routealarm.composeapp.generated.resources.default_transport
import routealarm.composeapp.generated.resources.metro
import routealarm.composeapp.generated.resources.metrobus
import routealarm.composeapp.generated.resources.minibus
import routealarm.composeapp.generated.resources.taxi
import routealarm.composeapp.generated.resources.train

@Serializable
enum class TransportType(val iconRes: DrawableResource) {
    BUS(Res.drawable.bus),
    METRO(Res.drawable.metro),
    MINIBUS(Res.drawable.minibus),
    METROBUS(Res.drawable.metrobus),
    TAXI(Res.drawable.taxi),
    TRAIN(Res.drawable.train),
    DEFAULT(Res.drawable.default_transport)
}

@Serializable
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

fun Stop.addPlace(place: Place?): Stop {
    return if (place == null )
        this
    else{
        this.copy(
            latitude = place.latitude,
            longitude = place.longitude,
            address = place.address
        )
    }

}
