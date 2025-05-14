package org.yusufteker.routealarm.feature.location.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import org.yusufteker.routealarm.feature.location.domain.Location
import android.location.Location as ApiLocation
import kotlin.coroutines.resume

actual class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    actual suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: ApiLocation? ->
                if (location != null) {
                    cont.resume(
                        Location(
                            name = "current location",
                            lat = location.latitude,
                            lng = location.longitude
                        )
                    )
                    Log.d("LocationService", "Current location: ${location.latitude} ${location.longitude}")
                } else {
                    Log.d("LocationService", "Location is null")
                    cont.resume(null)
                }
            }
            .addOnFailureListener {
                Log.e("LocationService", "Error getting location", it)
                cont.resume(null)
            }
    }
}