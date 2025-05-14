package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
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

    val clLocation = remember(currentLocation) {
        CLLocationCoordinate2DMake(
            currentLocation.lat,
            currentLocation.lng
        )
    }
    UIKitView(
        modifier = Modifier,
        factory = {
            MKMapView().apply {
                setZoomEnabled(true)
                setScrollEnabled(true)
                // customize map view further
            }
        },
        update = { mapView ->
            mapView.setCenterCoordinate(clLocation, animated = true)
        }
    )
}