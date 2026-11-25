package org.yusufteker.routealarm.feature.location.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location as ApiLocation
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.yusufteker.routealarm.feature.location.domain.Location
import kotlin.coroutines.resume

actual class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    actual suspend fun getCurrentLocation(): Location? {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            Napier.w("Location permission not granted", tag = "LocationService")
            return null
        }

        return withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                val cancellationTokenSource = CancellationTokenSource()
                val locationRequest = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdateAgeMillis(15_000L)
                    .build()

                fusedLocationClient.getCurrentLocation(locationRequest, cancellationTokenSource.token)
                    .addOnSuccessListener { location: ApiLocation? ->
                        if (!cont.isActive) return@addOnSuccessListener
                        if (location != null) {
                            cont.resume(
                                Location(
                                    name = "current location",
                                    lat = location.latitude,
                                    lng = location.longitude
                                )
                            )
                        } else {
                            // Fallback to lastLocation if getCurrentLocation returned null
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { lastLoc ->
                                    if (!cont.isActive) return@addOnSuccessListener
                                    cont.resume(
                                        lastLoc?.let {
                                            Location("current location", it.latitude, it.longitude)
                                        }
                                    )
                                }
                                .addOnFailureListener {
                                    if (cont.isActive) cont.resume(null)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Napier.e("Error getting location: ${e.message}", tag = "LocationService")
                        if (cont.isActive) cont.resume(null)
                    }

                cont.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        }
    }
}
