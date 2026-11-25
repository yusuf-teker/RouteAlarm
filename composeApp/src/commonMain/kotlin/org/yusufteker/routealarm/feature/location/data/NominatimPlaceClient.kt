package org.yusufteker.routealarm.feature.location.data

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.Place
import org.yusufteker.routealarm.feature.location.domain.calculateDistance
import org.yusufteker.routealarm.feature.location.domain.emptyLocation

@Serializable
internal data class NominatimAddress(
    val road: String? = null,
    val neighbourhood: String? = null,
    val suburb: String? = null,
    val quarter: String? = null,
    @SerialName("city_district") val cityDistrict: String? = null,
    val district: String? = null,
    val town: String? = null,
    val city: String? = null,
    val province: String? = null,
    val state: String? = null,
    val country: String? = null
)

@Serializable
internal data class NominatimSearchItem(
    @SerialName("place_id") val placeId: Long = 0L,
    val lat: String = "",
    val lon: String = "",
    val name: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val address: NominatimAddress? = null
)

@Serializable
internal data class NominatimReverseItem(
    val name: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val address: NominatimAddress? = null
)

@Serializable
internal data class PhotonResponse(
    val features: List<PhotonFeature> = emptyList()
)

@Serializable
internal data class PhotonFeature(
    val geometry: PhotonGeometry? = null,
    val properties: PhotonProperties? = null
)

@Serializable
internal data class PhotonGeometry(
    val coordinates: List<Double> = emptyList()
)

@Serializable
internal data class PhotonProperties(
    @SerialName("osm_id") val osmId: Long = 0L,
    val name: String? = null,
    val street: String? = null,
    val housenumber: String? = null,
    val locality: String? = null,
    val district: String? = null,
    val city: String? = null,
    val county: String? = null,
    val state: String? = null,
    val country: String? = null
)

internal class NominatimPlaceClient(
    private val httpClient: HttpClient
) {
    suspend fun searchFreePlaces(query: String, nearLocation: Location? = null): List<Place> = coroutineScope {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@coroutineScope emptyList()

        val photonDeferred = async { searchPhoton(trimmed, nearLocation) }
        val nominatimDeferred = async { searchPlaces(trimmed, nearLocation) }

        mergeAndRankPlaces(
            places = photonDeferred.await() + nominatimDeferred.await(),
            query = trimmed,
            nearLocation = nearLocation
        )
    }

    suspend fun searchPhoton(query: String, nearLocation: Location? = null): List<Place> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()
        val validCenter = nearLocation?.takeIf {
            it.lat != emptyLocation.lat || it.lng != emptyLocation.lng
        }

        return try {
            val response = httpClient.get("https://photon.komoot.io/api/") {
                url {
                    parameters.append("q", trimmed)
                    parameters.append("limit", "10")
                    if (validCenter != null) {
                        parameters.append("lat", validCenter.lat.toString())
                        parameters.append("lon", validCenter.lng.toString())
                        parameters.append("location_bias_scale", "0.5")
                    }
                }
                headers {
                    append(HttpHeaders.UserAgent, "RouteAlarm/1.0 (mobile-location-alarm)")
                    append(HttpHeaders.Accept, "application/json")
                }
            }
            if (!response.status.isSuccess()) {
                Napier.w("Photon search HTTP ${response.status.value}", tag = "PlaceSuggestion")
                return emptyList()
            }
            val body: PhotonResponse = response.body()
            body.features.mapNotNull { feature ->
                val coords = feature.geometry?.coordinates.orEmpty()
                if (coords.size < 2) return@mapNotNull null
                val lng = coords[0]
                val lat = coords[1]
                val props = feature.properties ?: return@mapNotNull null

                val streetFull = listOfNotNull(
                    props.street?.takeIf { it.isNotBlank() },
                    props.housenumber?.takeIf { it.isNotBlank() }
                ).joinToString(" ").takeIf { it.isNotBlank() }

                val title = props.name?.takeIf { it.isNotBlank() }
                    ?: streetFull
                    ?: props.district?.takeIf { it.isNotBlank() }
                    ?: props.city?.takeIf { it.isNotBlank() }
                    ?: trimmed

                val addressParts = listOfNotNull(
                    streetFull?.takeIf { it != title },
                    props.locality?.takeIf { it.isNotBlank() && it != title },
                    props.district?.takeIf { it.isNotBlank() && it != title },
                    props.city?.takeIf { it.isNotBlank() && it != title }
                        ?: props.county?.takeIf { it.isNotBlank() && it != title },
                    props.state?.takeIf { it.isNotBlank() && it != props.city },
                    props.country?.takeIf { it.isNotBlank() }
                ).distinct()

                Place(
                    id = "photon:${props.osmId}:$lat,$lng",
                    name = title,
                    address = addressParts.joinToString(", "),
                    latitude = lat,
                    longitude = lng
                )
            }
        } catch (e: Exception) {
            Napier.w("Photon search failed: ${e.message}", tag = "PlaceSuggestion")
            emptyList()
        }
    }
    suspend fun searchPlaces(query: String, nearLocation: Location? = null): List<Place> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()
        val validCenter = nearLocation?.takeIf {
            it.lat != emptyLocation.lat || it.lng != emptyLocation.lng
        }

        return try {
            val response = httpClient.get("https://nominatim.openstreetmap.org/search") {
                url {
                    parameters.append("q", trimmed)
                    parameters.append("format", "jsonv2")
                    parameters.append("addressdetails", "1")
                    parameters.append("limit", "10")
                    if (validCenter != null) {
                        val delta = 0.45
                        val left = validCenter.lng - delta
                        val top = validCenter.lat + delta
                        val right = validCenter.lng + delta
                        val bottom = validCenter.lat - delta
                        parameters.append("viewbox", "$left,$top,$right,$bottom")
                        parameters.append("bounded", "0")
                    }
                }
                headers {
                    append(HttpHeaders.UserAgent, "RouteAlarm/1.0 (mobile-location-alarm)")
                    append(HttpHeaders.Accept, "application/json")
                }
            }
            if (!response.status.isSuccess()) {
                Napier.w("Nominatim search HTTP ${response.status.value}", tag = "PlaceSuggestion")
                return emptyList()
            }
            val items: List<NominatimSearchItem> = response.body()
            val mapped = items.mapNotNull { item ->
                val lat = item.lat.toDoubleOrNull() ?: return@mapNotNull null
                val lng = item.lon.toDoubleOrNull() ?: return@mapNotNull null
                val displayParts = item.displayName
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    .orEmpty()
                val title = item.name?.takeIf { it.isNotBlank() }
                    ?: formatShortName(item.address)
                    ?: displayParts.firstOrNull()
                    ?: trimmed
                val address = formatAddress(item.address)
                    ?: displayParts.drop(1).take(3).joinToString(", ")

                Place(
                    id = "osm:${item.placeId}:$lat,$lng",
                    name = title,
                    address = address,
                    latitude = lat,
                    longitude = lng
                )
            }
            if (validCenter != null) {
                mapped.sortedBy {
                    calculateDistance(validCenter.lat, validCenter.lng, it.latitude, it.longitude)
                }.take(8)
            } else {
                mapped.take(8)
            }
        } catch (e: Exception) {
            Napier.w("Nominatim search failed: ${e.message}", tag = "PlaceSuggestion")
            emptyList()
        }
    }

    suspend fun reverseGeocode(lat: Double, lng: Double): String? {
        return try {
            val response = httpClient.get("https://nominatim.openstreetmap.org/reverse") {
                url {
                    parameters.append("lat", lat.toString())
                    parameters.append("lon", lng.toString())
                    parameters.append("format", "jsonv2")
                    parameters.append("addressdetails", "1")
                    parameters.append("zoom", "18")
                }
                headers {
                    append(HttpHeaders.UserAgent, "RouteAlarm/1.0 (mobile-location-alarm)")
                    append(HttpHeaders.Accept, "application/json")
                }
            }
            if (!response.status.isSuccess()) return null
            val item: NominatimReverseItem = response.body()
            item.name?.takeIf { it.isNotBlank() }
                ?: formatShortName(item.address)
                ?: item.displayName
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.take(2)
                    ?.joinToString(", ")
                    ?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Napier.w("Nominatim reverse failed: ${e.message}", tag = "PlaceSuggestion")
            null
        }
    }

    private fun formatShortName(address: NominatimAddress?): String? {
        if (address == null) return null
        val primary = address.road?.takeIf { it.isNotBlank() }
            ?: address.neighbourhood?.takeIf { it.isNotBlank() }
            ?: address.quarter?.takeIf { it.isNotBlank() }
            ?: address.suburb?.takeIf { it.isNotBlank() }
            ?: address.town?.takeIf { it.isNotBlank() }
            ?: address.city?.takeIf { it.isNotBlank() }
        val area = address.suburb?.takeIf { it.isNotBlank() && it != primary }
            ?: address.cityDistrict?.takeIf { it.isNotBlank() && it != primary }
            ?: address.district?.takeIf { it.isNotBlank() && it != primary }
            ?: address.city?.takeIf { it.isNotBlank() && it != primary }
        return listOfNotNull(primary, area).joinToString(", ").takeIf { it.isNotBlank() }
    }

    private fun formatAddress(address: NominatimAddress?): String? {
        if (address == null) return null
        val parts = listOfNotNull(
            address.road?.takeIf { it.isNotBlank() },
            address.suburb?.takeIf { it.isNotBlank() }
                ?: address.neighbourhood?.takeIf { it.isNotBlank() },
            address.cityDistrict?.takeIf { it.isNotBlank() }
                ?: address.district?.takeIf { it.isNotBlank() }
                ?: address.town?.takeIf { it.isNotBlank() },
            address.city?.takeIf { it.isNotBlank() }
                ?: address.province?.takeIf { it.isNotBlank() }
                ?: address.state?.takeIf { it.isNotBlank() },
            address.country?.takeIf { it.isNotBlank() }
        ).distinct()
        return parts.joinToString(", ").takeIf { it.isNotBlank() }
    }
}

internal fun mergeAndRankPlaces(
    places: List<Place>,
    query: String,
    nearLocation: Location?,
    limit: Int = 8
): List<Place> {
    if (places.isEmpty()) return emptyList()

    val validCenter = nearLocation?.takeIf {
        it.lat != emptyLocation.lat || it.lng != emptyLocation.lng
    }
    val queryTokens = query.trim().lowercase().split(" ").filter { it.isNotBlank() }

    fun score(place: Place): Double {
        val hay = "${place.name} ${place.address}".lowercase()
        val exactMatchBonus = if (place.name.lowercase().contains(query.trim().lowercase())) -50.0 else 0.0
        val matchedTokens = queryTokens.count { hay.contains(it) }
        val tokenPenalty = (queryTokens.size - matchedTokens) * 25.0
        val distanceKm = if (validCenter != null) {
            calculateDistance(validCenter.lat, validCenter.lng, place.latitude, place.longitude) / 1000.0
        } else {
            0.0
        }
        return exactMatchBonus + tokenPenalty + distanceKm
    }

    val deduped = mutableListOf<Place>()
    for (candidate in places.sortedBy(::score)) {
        val duplicate = deduped.any { existing ->
            val distance = calculateDistance(
                existing.latitude,
                existing.longitude,
                candidate.latitude,
                candidate.longitude
            )
            distance < 35.0 || (distance < 120.0 && existing.name.equals(candidate.name, ignoreCase = true))
        }
        if (!duplicate) {
            deduped.add(candidate)
            if (deduped.size >= limit) break
        }
    }
    return deduped
}

