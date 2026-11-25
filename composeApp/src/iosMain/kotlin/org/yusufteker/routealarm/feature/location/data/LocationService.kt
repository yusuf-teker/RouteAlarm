package org.yusufteker.routealarm.feature.location.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.yusufteker.routealarm.feature.location.domain.Location
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSinceNow
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class LocationService {

    private var activeManager: CLLocationManager? = null
    private var activeDelegate: CLLocationManagerDelegateProtocol? = null

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): Location? {
        return withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                val manager = CLLocationManager()
                manager.desiredAccuracy = kCLLocationAccuracyBest

                // Check if cached recent location is already fresh (< 15 seconds)
                val cached = manager.location
                if (cached != null && cached.timestamp.timeIntervalSinceNow > -15.0) {
                    val (lat, lng) = cached.coordinate.useContents { latitude to longitude }
                    if (lat != 0.0 || lng != 0.0) {
                        cont.resume(Location(name = "current location", lat = lat, lng = lng))
                        return@suspendCancellableCoroutine
                    }
                }

                val status = manager.authorizationStatus
                if (status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted) {
                    cont.resume(null)
                    return@suspendCancellableCoroutine
                }

                val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                        val loc = didUpdateLocations.lastOrNull() as? CLLocation ?: return
                        val (lat, lng) = loc.coordinate.useContents { latitude to longitude }
                        manager.stopUpdatingLocation()
                        cleanup()
                        if (cont.isActive) {
                            cont.resume(Location(name = "current location", lat = lat, lng = lng))
                        }
                    }

                    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                        manager.stopUpdatingLocation()
                        cleanup()
                        if (cont.isActive) {
                            cont.resume(null)
                        }
                    }

                    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                        val newStatus = manager.authorizationStatus
                        if (newStatus == kCLAuthorizationStatusDenied || newStatus == kCLAuthorizationStatusRestricted) {
                            manager.stopUpdatingLocation()
                            cleanup()
                            if (cont.isActive) {
                                cont.resume(null)
                            }
                        }
                    }
                }

                activeManager = manager
                activeDelegate = delegate
                manager.delegate = delegate
                manager.startUpdatingLocation()

                cont.invokeOnCancellation {
                    manager.stopUpdatingLocation()
                    cleanup()
                }
            }
        }
    }

    private fun cleanup() {
        activeManager?.delegate = null
        activeManager = null
        activeDelegate = null
    }
}
