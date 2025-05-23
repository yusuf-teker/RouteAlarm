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

interface UpdatableMapViewFactory : NativeViewFactory {
    fun updateCurrentLocation(location: Location)
    fun updateSelectedLocation(location: Location?)
    fun updateCenterToCurrentLocation(shouldCenter: Boolean)
}