package org.yusufteker.routealarm.feature.location.data

import org.yusufteker.routealarm.feature.location.domain.Location

expect class LocationService {
    suspend fun getCurrentLocation(): Location?
}