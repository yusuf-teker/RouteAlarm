package org.yusufteker.routealarm.feature.location.data

import org.yusufteker.routealarm.feature.location.domain.Location

interface LocationRepository {
    suspend fun searchLocations(query: String): LocationSearchResult
    suspend fun getCurrentLocation(): Location?
    suspend fun getLocationFromMap(lat: Double, lon: Double): Location?
    suspend fun saveLocation(location: Location)
    suspend fun getSavedLocations(): List<Location>
}