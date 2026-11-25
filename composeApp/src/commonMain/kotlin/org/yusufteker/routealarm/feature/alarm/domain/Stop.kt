package org.yusufteker.routealarm.feature.alarm.domain

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.yusufteker.routealarm.feature.location.domain.Place
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.bus
import routealarm.composeapp.generated.resources.car
import routealarm.composeapp.generated.resources.default_transport
import routealarm.composeapp.generated.resources.metro
import routealarm.composeapp.generated.resources.metrobus
import routealarm.composeapp.generated.resources.minibus
import routealarm.composeapp.generated.resources.taxi
import routealarm.composeapp.generated.resources.train
import routealarm.composeapp.generated.resources.transport_bus
import routealarm.composeapp.generated.resources.transport_car
import routealarm.composeapp.generated.resources.transport_metro
import routealarm.composeapp.generated.resources.transport_metrobus
import routealarm.composeapp.generated.resources.transport_minibus
import routealarm.composeapp.generated.resources.transport_taxi
import routealarm.composeapp.generated.resources.transport_train

@Serializable
enum class TransportType(
    val iconRes: DrawableResource,
    val labelRes: StringResource
) {
    BUS(Res.drawable.bus, Res.string.transport_bus),
    METRO(Res.drawable.metro, Res.string.transport_metro),
    MINIBUS(Res.drawable.minibus, Res.string.transport_minibus),
    METROBUS(Res.drawable.metrobus, Res.string.transport_metrobus),
    CAR(Res.drawable.car, Res.string.transport_car),
    TAXI(Res.drawable.taxi, Res.string.transport_taxi),
    TRAIN(Res.drawable.train, Res.string.transport_train),
    DEFAULT(Res.drawable.default_transport, Res.string.transport_bus)
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
            name = place.name,
            latitude = place.latitude,
            longitude = place.longitude,
            address = place.address
        )
    }

}
