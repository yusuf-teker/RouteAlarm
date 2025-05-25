package org.yusufteker.routealarm.feature.location.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import org.yusufteker.routealarm.feature.location.domain.Place
// iosMain
import platform.MapKit.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


actual class PlaceSuggestionService {

    actual suspend fun getSuggestions(query: String): List<Place> {
        return try {
            // Farklı arama türleriyle daha fazla sonuç alalım
            val allResults = mutableListOf<Place>()

            // 1. Point of Interest arama
            val poiResults = searchWithType(query, MKLocalSearchResultTypePointOfInterest)
            allResults.addAll(poiResults)

            // 2. Address arama
            val addressResults = searchWithType(query, MKLocalSearchResultTypeAddress)
            allResults.addAll(addressResults)

            // Duplikatları temizle ve sınırla
            allResults.distinctBy { it.id }.take(6)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getPlaceDetails(placeId: String): Place? {
        return null
    }


    @OptIn(ExperimentalForeignApi::class)
    private suspend fun searchWithType(query: String, resultType: platform.MapKit.MKLocalSearchResultType): List<Place> =
        suspendCancellableCoroutine { continuation ->
            val request = MKLocalSearchRequest().apply {
                naturalLanguageQuery = query
                resultTypes = resultType
                // Global arama için region belirtme
            }

            val search = MKLocalSearch(request)
            search.startWithCompletionHandler { response, error ->
                if (error != null) {
                    continuation.resume(emptyList())
                    return@startWithCompletionHandler
                }

                val places = response?.mapItems?.mapNotNull { mapItem ->
                    val item = mapItem as MKMapItem
                    val coordinate = item.placemark.coordinate
                    Place(
                        id = "${coordinate.useContents { latitude }},${coordinate.useContents { longitude }}",
                        name = item.name ?: item.placemark.title ?: "Bilinmeyen Konum",
                        address = formatAddress(item.placemark),
                        latitude = coordinate.useContents { latitude },
                        longitude = coordinate.useContents { longitude }
                    )
                } ?: emptyList()

                continuation.resume(places)
            }
        }

    private fun formatAddress(placemark: MKPlacemark): String {
        val components = mutableListOf<String>()

        placemark.thoroughfare?.let { components.add(it) }
        placemark.subThoroughfare?.let { components.add(it) }
        placemark.locality?.let { components.add(it) }
        placemark.administrativeArea?.let { components.add(it) }
        placemark.country?.let { components.add(it) }

        return if (components.isNotEmpty()) {
            components.joinToString(", ")
        } else {
            "Adres bilgisi yok"
        }
    }
}
