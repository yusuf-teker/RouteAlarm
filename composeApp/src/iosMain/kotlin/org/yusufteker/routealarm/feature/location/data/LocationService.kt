package org.yusufteker.routealarm.feature.location.data

import org.yusufteker.routealarm.feature.location.domain.Location

//TODO IMPLEMENTATION EKLENECEK
actual class LocationService  constructor() {
    actual suspend fun getCurrentLocation(): Location? {
        // Implement using CoreLocation
        return null
    }

    actual suspend fun searchLocations(query: String): LocationSearchResult {
        // Implement using MapKit local search
        return LocationSearchResult(emptyList(), true)
    }

    actual suspend fun reverseGeocode(lat: Double, lon: Double): Location? {
        return null
        // Implement using CLGeocoder
    }
}