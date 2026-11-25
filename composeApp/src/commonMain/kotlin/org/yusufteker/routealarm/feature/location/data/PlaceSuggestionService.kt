package org.yusufteker.routealarm.feature.location.data

import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.Place

expect class PlaceSuggestionService {
    suspend fun getSuggestions(query: String, nearLocation: Location? = null): List<Place>
    suspend fun getPlaceDetails(placeId: String): Place?
    suspend fun reverseGeocode(lat: Double, lng: Double): String?
}