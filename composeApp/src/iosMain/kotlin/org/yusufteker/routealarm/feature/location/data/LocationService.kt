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
                if (location != null) {
                    val (lat, lng) = location.coordinate.useContents {
                        latitude to longitude
                    }
                    cont.resume(Location(name= "current location",lat = lat, lng = lng))
                    manager.stopUpdatingLocation()
                } else {
                    cont.resume(null)
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                cont.resume(null)
            }
        }

        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
    }
}
