package org.yusufteker.routealarm

import org.yusufteker.routealarm.feature.location.domain.Location

import platform.UIKit.UIViewController

interface NativeViewFactory {
    fun createGoogleMapView(
        selectedLocation: Location?,
        currentLocation: Location,
        onLocationSelected: (Location) -> Unit,
        centerToCurrentLocation: Boolean,
        onCenterLocationConsumed: () -> Unit
    ): UIViewController


}