package org.yusufteker.routealarm.feature.location.data

import org.yusufteker.routealarm.feature.location.domain.Location


data class LocationSearchResult(
    val locations: List<Location>,
    val isComplete: Boolean
)