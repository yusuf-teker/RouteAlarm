package org.yusufteker.routealarm.feature.location.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.yusufteker.routealarm.feature.location.domain.Location
import platform.CoreLocation.*
import kotlin.coroutines.resume
import platform.darwin.NSObject
import platform.Foundation.NSError
import kotlinx.cinterop.useContents

actual class LocationService {

    private val locationManager = CLLocationManager()

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = (didUpdateLocations.firstOrNull() as? CLLocation)
                if (location != null && !cont.isCompleted) {
                    val (lat, lng) = location.coordinate.useContents {
                        latitude to longitude
                    }
                    cont.resume(Location(name = "current location", lat = lat, lng = lng))
                    manager.stopUpdatingLocation()
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                if (!cont.isCompleted) {
                    cont.resume(null)
                }
            }
        }

        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
    }
}
