package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController

import kotlinx.cinterop.ExperimentalForeignApi
import org.yusufteker.routealarm.LocalMapViewFactory
import org.yusufteker.routealarm.feature.location.domain.Location
import platform.UIKit.UIViewController

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
    val factory = LocalMapViewFactory.current
    val mapViewController = remember { mutableStateOf<UIViewController?>(null) }

    UIKitViewController(
        modifier = modifier,
        factory = {
            factory.createMapView(
                initialLocation = currentLocation,
                onMapClick = { lat, lng ->
                    onLocationSelected(
                        Location("Seçilen Konum", lat, lng)
                    )
                }
            ).also {
                mapViewController.value = it
            }
        },
        update = { controller ->
            // Controller'dan MapController referansını al
            val mapController = controller.mapController

            // selectedLocation değiştiğinde haritayı güncelle
            selectedLocation?.let { location ->
                mapController?.updateSelectedLocation(location)
            }

            // Eğer center to current location true ise, current location'a git
            if (centerToCurrentLocation) {
                mapController?.centerToLocation(currentLocation)
                onCenterLocationConsumed()
            }
        }
    )

}