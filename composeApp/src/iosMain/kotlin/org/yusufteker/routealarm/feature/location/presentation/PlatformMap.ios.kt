package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.yusufteker.routealarm.feature.location.domain.Location

@Composable
actual fun PlatformMap(
    modifier: Modifier,
    selectedLocation: Location?,
    currentLocation: Location,
    onLocationSelected: (Location) -> Unit,
    centerToCurrentLocation: Boolean,
    onCenterLocationConsumed: () -> Unit

) {

}