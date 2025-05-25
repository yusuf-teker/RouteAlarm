package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import org.yusufteker.routealarm.LocalNativeViewFactory
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.UpdatableMapViewFactory

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformMap(
    modifier: Modifier,
    selectedLocation: Location,
    currentLocation: Location,
    onLocationSelected: (Location) -> Unit,
    centerToCurrentLocation: Boolean,
    onCenterLocationConsumed: () -> Unit

) {

    val factory = LocalNativeViewFactory.current
    val updatableFactory = factory as? UpdatableMapViewFactory

    LaunchedEffect(currentLocation) {
        println("PlatformMap: currentLocation değişti: $currentLocation")
        updatableFactory?.updateCurrentLocation(currentLocation)
    }

    LaunchedEffect(selectedLocation) {
        println("PlatformMap: selectedLocation değişti: $selectedLocation")
        updatableFactory?.updateSelectedLocation(selectedLocation)
    }

    LaunchedEffect(centerToCurrentLocation) {
        println("PlatformMap: centerToCurrentLocation değişti: $centerToCurrentLocation")
        updatableFactory?.updateCenterToCurrentLocation(centerToCurrentLocation)
    }



    UIKitViewController(
        modifier = modifier,
        factory = {
            factory.createGoogleMapView(
                selectedLocation = selectedLocation,
                currentLocation = currentLocation,
                onLocationSelected = onLocationSelected,
                centerToCurrentLocation = centerToCurrentLocation,
                onCenterLocationConsumed = onCenterLocationConsumed
            )
        },
        update = {

        }

    )


}