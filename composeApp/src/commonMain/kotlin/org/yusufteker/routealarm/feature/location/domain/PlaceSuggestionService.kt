package org.yusufteker.routealarm.feature.location.domain

expect class PlaceSuggestionService {
    suspend fun getSuggestions(query: String): List<Place>
    suspend fun getPlaceDetails(placeId: String): Place?
}