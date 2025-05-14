package org.yusufteker.routealarm.feature.location.presentation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.yusufteker.routealarm.feature.location.domain.Location

@Composable
actual fun PlatformMap(
    modifier: Modifier,
    selectedLocation: Location,
    onLocationSelected: (Location) -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            selectedLocation.toLatLng(),
            12f
        )
    }
    LaunchedEffect(cameraPositionState.isMoving) {
        Log.d("MAP_STATE", "Is Moving: ${cameraPositionState.isMoving}")
    }
    LaunchedEffect(selectedLocation) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(selectedLocation.toLatLng(), 15f),
            durationMs = 1000 // 1 saniyelik animasyon
        )

    }
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            onLocationSelected(
                Location("Seçilen Konum", latLng.latitude, latLng.longitude)
            )

        },
        onMapLoaded = {
            Log.d("PlatformMap - Android", "Map loaded!")
        }
    ) {
        selectedLocation?.let {
            Marker(
                state = MarkerState(position = it.toLatLng()),
                title = it.name
            )
        }
    }
}

private fun Location.toLatLng(): LatLng {
    return LatLng(lat, lng)
}
