package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.ui.uikit.ComposeUIViewControllerDelegate
import kotlinx.cinterop.ExperimentalForeignApi
import org.yusufteker.routealarm.feature.location.domain.Location
import platform.UIKit.UIViewController
import platform.objc.objc_getAssociatedObject
import platform.objc.sel_registerName

interface PlatformMapFactory {
    fun createMapView(
        initialLocation: Location,
        onMapClick: (Double, Double) -> Unit,
    ): UIViewController
}

interface MapController {
    fun updateSelectedLocation(location: Location)
    fun centerToLocation(location: Location)
}


// UIViewController için extension - Swift tarafında tanımlanan mapController'a erişim sağlar
@OptIn(ExperimentalForeignApi::class)
val UIViewController.mapController: MapController?
    get() {
        return objc_getAssociatedObject(this, sel_registerName("mapControllerKey")) as? MapController
    }