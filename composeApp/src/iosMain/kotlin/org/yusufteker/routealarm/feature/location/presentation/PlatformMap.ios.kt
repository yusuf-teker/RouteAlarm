package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import org.yusufteker.routealarm.LocalNativeViewFactory
import org.yusufteker.routealarm.feature.location.domain.Location
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKMapView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformMap(
    modifier: Modifier,
    selectedLocation: Location?,
    currentLocation: Location,
    onLocationSelected: (Location) -> Unit,
    centerToCurrentLocation: Boolean,
    onCenterLocationConsumed: () -> Unit

) {

    val factory = LocalNativeViewFactory.current

    LaunchedEffect(currentLocation){
        print("yusuf $currentLocation")
    }


    val mapView = factory.createGoogleMapView(
        selectedLocation = selectedLocation,
        currentLocation = currentLocation,
        onLocationSelected = onLocationSelected,
        centerToCurrentLocation = centerToCurrentLocation,
        onCenterLocationConsumed = onCenterLocationConsumed
    )
    UIKitViewController(
        modifier = modifier,
        factory = {
            mapView
        }


    )


}