package org.yusufteker.routealarm.feature.location.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.emptyLocation

@Composable
actual fun PlatformMap(
    modifier: Modifier,
    selectedLocation: Location,
    currentLocation: Location,
    onLocationSelected: (Location) -> Unit,
    centerToCurrentLocation: Boolean,
    onCenterLocationConsumed: () -> Unit,
) {
    val context = LocalContext.current
    val hasLocationPermission = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    val initialTarget = if (currentLocation != emptyLocation) {
        currentLocation.toLatLng()
    } else {
        LatLng(41.0082, 28.9784) // Sensible default before GPS fix arrives
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialTarget, 15f)
    }

    var hasCenteredInitially by remember { mutableStateOf(currentLocation != emptyLocation) }

    LaunchedEffect(centerToCurrentLocation) {
        if (centerToCurrentLocation) {
            if (currentLocation != emptyLocation) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(currentLocation.toLatLng(), 15f),
                    durationMs = 800
                )
            }
            onCenterLocationConsumed()
        }
    }

    val markerState = rememberMarkerState(position = selectedLocation.toLatLng())

    LaunchedEffect(selectedLocation) {
        if (selectedLocation != emptyLocation) {
            markerState.position = selectedLocation.toLatLng()
            val targetZoom = maxOf(cameraPositionState.position.zoom, 15f)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(selectedLocation.toLatLng(), targetZoom),
                durationMs = 800
            )
        }
    }

    LaunchedEffect(currentLocation) {
        if (!hasCenteredInitially && currentLocation != emptyLocation) {
            hasCenteredInitially = true
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(currentLocation.toLatLng(), 15f),
                durationMs = 800
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
        uiSettings = MapUiSettings(myLocationButtonEnabled = false),
        onMapClick = { latLng ->
            onLocationSelected(
                Location("", latLng.latitude, latLng.longitude)
            )
        },
        onMapLongClick = { latLng ->
            onLocationSelected(
                Location("", latLng.latitude, latLng.longitude)
            )
        },
        onPOIClick = { poi ->
            onLocationSelected(
                Location(
                    name = poi.name.replace('\n', ' ').trim(),
                    lat = poi.latLng.latitude,
                    lng = poi.latLng.longitude
                )
            )
        }
    ) {
        if (selectedLocation != emptyLocation) {
            Marker(
                state = markerState,
                title = selectedLocation.name.ifBlank { null }
            )
        }
    }
}

private fun Location.toLatLng(): LatLng = LatLng(lat, lng)
