package org.yusufteker.routealarm.feature.location.data

// iosMain
actual class PlaceSuggestionService {

    actual suspend fun getSuggestions(query: String): List<Place> {
        // TODO: Apple MapKit veya başka iOS API ile entegre et
        return emptyList()
    }

    actual suspend fun getPlaceDetails(placeId: String): Place? {
        // TODO: ID'ye göre detay alma işlemi
        return null
    }
}