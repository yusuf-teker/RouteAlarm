package org.yusufteker.routealarm.feature.location.data

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.Place
import org.yusufteker.routealarm.feature.location.domain.emptyLocation
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLPlacemark
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKLocalSearch
import platform.MapKit.MKLocalSearchRequest
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark
import kotlin.coroutines.resume

actual class PlaceSuggestionService(
    httpClient: HttpClient
) {
    private val nominatimClient = NominatimPlaceClient(httpClient)

    actual suspend fun getSuggestions(query: String, nearLocation: Location?): List<Place> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()

        val mapKitResults = try {
            searchMapKit(trimmed, nearLocation)
        } catch (e: Exception) {
            Napier.w("MKLocalSearch failed: ${e.message}", tag = "PlaceSuggestion")
            emptyList()
        }

        if (mapKitResults.isNotEmpty()) {
            return mapKitResults.distinctBy { it.id }.take(8)
        }

        return nominatimClient.searchFreePlaces(trimmed, nearLocation)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getPlaceDetails(placeId: String): Place? {
        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun reverseGeocode(lat: Double, lng: Double): String? {
        val nativeName = suspendCancellableCoroutine { continuation ->
            val geocoder = CLGeocoder()
            val location = CLLocation(latitude = lat, longitude = lng)
            geocoder.reverseGeocodeLocation(location) { placemarks, error ->
                if (!continuation.isActive) return@reverseGeocodeLocation
                if (error != null) {
                    continuation.resume(null)
                    return@reverseGeocodeLocation
                }
                val placemark = placemarks?.firstOrNull() as? CLPlacemark
                val primary = placemark?.thoroughfare?.takeIf { it.isNotBlank() }
                    ?: placemark?.name?.takeIf { it.isNotBlank() }
                val area = placemark?.subLocality?.takeIf { it.isNotBlank() && it != primary }
                    ?: placemark?.locality?.takeIf { it.isNotBlank() && it != primary }
                val resolved = listOfNotNull(primary, area)
                    .joinToString(", ")
                    .takeIf { it.isNotBlank() }
                continuation.resume(resolved)
            }
        }

        return nativeName ?: nominatimClient.reverseGeocode(lat, lng)
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun searchMapKit(query: String, nearLocation: Location?): List<Place> =
        suspendCancellableCoroutine { continuation ->
            val request = MKLocalSearchRequest().apply {
                naturalLanguageQuery = query
                if (nearLocation != null &&
                    (nearLocation.lat != emptyLocation.lat || nearLocation.lng != emptyLocation.lng)
                ) {
                    region = MKCoordinateRegionMakeWithDistance(
                        centerCoordinate = CLLocationCoordinate2DMake(nearLocation.lat, nearLocation.lng),
                        latitudinalMeters = 30_000.0,
                        longitudinalMeters = 30_000.0
                    )
                }
            }

            val search = MKLocalSearch(request)
            continuation.invokeOnCancellation {
                search.cancel()
            }
            search.startWithCompletionHandler { response, error ->
                if (!continuation.isActive) return@startWithCompletionHandler
                if (error != null) {
                    continuation.resume(emptyList())
                    return@startWithCompletionHandler
                }

                val places = response?.mapItems?.mapNotNull { mapItem ->
                    val item = mapItem as? MKMapItem ?: return@mapNotNull null
                    val lat = item.placemark.coordinate.useContents { latitude }
                    val lng = item.placemark.coordinate.useContents { longitude }
                    Place(
                        id = "$lat,$lng",
                        name = item.name ?: item.placemark.title ?: "",
                        address = formatAddress(item.placemark),
                        latitude = lat,
                        longitude = lng
                    )
                }?.filter { it.name.isNotBlank() } ?: emptyList()

                continuation.resume(places)
            }
        }

    private fun formatAddress(placemark: MKPlacemark): String {
        val components = listOfNotNull(
            placemark.thoroughfare?.takeIf { it.isNotBlank() },
            placemark.subLocality?.takeIf { it.isNotBlank() },
            placemark.locality?.takeIf { it.isNotBlank() },
            placemark.administrativeArea?.takeIf { it.isNotBlank() },
            placemark.country?.takeIf { it.isNotBlank() }
        ).distinct()

        return components.joinToString(", ")
    }
}
