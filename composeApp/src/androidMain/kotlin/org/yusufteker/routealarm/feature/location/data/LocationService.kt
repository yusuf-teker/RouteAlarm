package org.yusufteker.routealarm.feature.location.data

import android.content.Context
import android.location.Geocoder
import org.yusufteker.routealarm.feature.location.domain.Location
//Todo Implementation sonra yapılacak
actual class LocationService  constructor(
    private val context: Context
) {
    private val geocoder = Geocoder(context)

    actual suspend fun getCurrentLocation(): Location? {
        // Implement using FusedLocationProviderClient

        return null
    }

    actual suspend fun searchLocations(query: String): LocationSearchResult {
        // Implement using Places API
        return LocationSearchResult(emptyList(), true)
    }

    actual suspend fun reverseGeocode(lat: Double, lon: Double): Location? {
        // Implement using Geocoder
        return null
    }
}