package org.yusufteker.routealarm.permissions


import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)

class IOSLocationPermissionsHandler : PermissionsBridgeListener {
    private val locationManager = CLLocationManager()
    private var pendingCallback: PermissionResultCallback? = null
    private var requestingBackgroundPermission = false

    private val locationDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val callback = pendingCallback ?: return

            when (manager.authorizationStatus) {
                kCLAuthorizationStatusAuthorizedAlways -> {
                    // Her zaman (background dahil) izin var
                    callback.onPermissionGranted()
                }
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    // Sadece uygulama kullanımdayken izin var
                    if (requestingBackgroundPermission) {
                        // Arkaplan izni istendi ama sadece foreground izni verilmiş
                        callback.onPermissionDenied(isPermanentDenied = false)
                    } else {
                        // Foreground izni istendi ve verilmiş
                        callback.onPermissionGranted()
                    }
                }
                kCLAuthorizationStatusDenied,
                kCLAuthorizationStatusRestricted -> {
                    callback.onPermissionDenied(isPermanentDenied = true)
                }
                else -> callback.onPermissionDenied(isPermanentDenied = false)
            }

            pendingCallback = null
            requestingBackgroundPermission = false
        }
    }

    init {
        locationManager.delegate = locationDelegate
    }

    // Foreground (uygulama açıkken) konum izni iste
    override fun requestLocationPermission(callback: PermissionResultCallback) {
        pendingCallback = callback
        requestingBackgroundPermission = false

        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                callback.onPermissionGranted()
                pendingCallback = null
            }
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                callback.onPermissionDenied(isPermanentDenied = true)
                pendingCallback = null
            }
            kCLAuthorizationStatusNotDetermined -> {
                locationManager.requestWhenInUseAuthorization()
            }
            else -> {
                callback.onPermissionDenied(isPermanentDenied = false)
                pendingCallback = null
            }
        }
    }

    // Yeni metod: Background konum izni iste
    fun requestBackgroundLocationPermission(callback: PermissionResultCallback) {
        pendingCallback = callback
        requestingBackgroundPermission = true

        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways -> {
                callback.onPermissionGranted()
                pendingCallback = null
                requestingBackgroundPermission = false
            }
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusNotDetermined,
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                // Her durumda Always izni iste
                locationManager.requestAlwaysAuthorization()
            }
            else -> {
                callback.onPermissionDenied(isPermanentDenied = false)
                pendingCallback = null
                requestingBackgroundPermission = false
            }
        }
    }

    // Background izni verilip verilmediğini kontrol et
    fun isBackgroundLocationPermissionGranted(): Boolean {
        return locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways
    }

    // Foreground izni verilip verilmediğini kontrol et
    override fun isLocationPermissionGranted(): Boolean {
        return when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> true
            else -> false
        }
    }
}