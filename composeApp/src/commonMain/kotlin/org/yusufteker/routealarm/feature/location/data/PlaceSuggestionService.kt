package org.yusufteker.routealarm.feature.location.data

import org.yusufteker.routealarm.feature.location.domain.Place

expect class PlaceSuggestionService {
    suspend fun getSuggestions(query: String): List<Place>
    suspend fun getPlaceDetails(placeId: String): Place?
}