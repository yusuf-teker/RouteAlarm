package org.yusufteker.routealarm.permissions

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
class IOSLocationPermissionsHandler : PermissionsBridgeListener {
    private val locationManager = CLLocationManager()
    private var pendingCallback: PermissionResultCallback? = null
    private var requestingBackgroundPermission = false
    private var hasRequestedAlwaysUpgrade = false
    private var cachedNotificationGranted: Boolean = false

    private val locationDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val callback = pendingCallback ?: return

            when (manager.authorizationStatus) {
                kCLAuthorizationStatusAuthorizedAlways -> {
                    pendingCallback = null
                    requestingBackgroundPermission = false
                    callback.onPermissionGranted()
                }
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    if (requestingBackgroundPermission) {
                        if (!hasRequestedAlwaysUpgrade) {
                            hasRequestedAlwaysUpgrade = true
                            manager.requestAlwaysAuthorization()
                        } else {
                            pendingCallback = null
                            requestingBackgroundPermission = false
                            callback.onPermissionDenied(isPermanentDenied = false)
                        }
                    } else {
                        pendingCallback = null
                        callback.onPermissionGranted()
                    }
                }
                kCLAuthorizationStatusDenied,
                kCLAuthorizationStatusRestricted -> {
                    pendingCallback = null
                    requestingBackgroundPermission = false
                    callback.onPermissionDenied(isPermanentDenied = true)
                }
                kCLAuthorizationStatusNotDetermined -> {
                    // Still waiting for user selection
                }
                else -> {
                    pendingCallback = null
                    requestingBackgroundPermission = false
                    callback.onPermissionDenied(isPermanentDenied = false)
                }
            }
        }
    }

    init {
        locationManager.delegate = locationDelegate
        checkNotificationPermission { }
    }

    override fun requestLocationPermission(callback: PermissionResultCallback) {
        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                callback.onPermissionGranted()
            }
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                callback.onPermissionDenied(isPermanentDenied = true)
            }
            kCLAuthorizationStatusNotDetermined -> {
                pendingCallback = callback
                requestingBackgroundPermission = false
                locationManager.requestWhenInUseAuthorization()
            }
            else -> {
                callback.onPermissionDenied(isPermanentDenied = false)
            }
        }
    }

    override fun requestBackgroundLocationPermission(callback: PermissionResultCallback) {
        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways -> {
                callback.onPermissionGranted()
            }
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                callback.onPermissionDenied(isPermanentDenied = true)
            }
            kCLAuthorizationStatusAuthorizedWhenInUse -> {
                if (!hasRequestedAlwaysUpgrade) {
                    hasRequestedAlwaysUpgrade = true
                    pendingCallback = callback
                    requestingBackgroundPermission = true
                    locationManager.requestAlwaysAuthorization()
                } else {
                    callback.onPermissionDenied(isPermanentDenied = true)
                }
            }
            kCLAuthorizationStatusNotDetermined -> {
                pendingCallback = callback
                requestingBackgroundPermission = true
                locationManager.requestWhenInUseAuthorization()
            }
            else -> {
                callback.onPermissionDenied(isPermanentDenied = false)
            }
        }
    }

    override fun isBackgroundLocationPermissionGranted(): Boolean {
        return locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways
    }

    override fun isLocationPermissionGranted(): Boolean {
        return when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> true
            else -> false
        }
    }

    override fun requestNotificationPermission(callback: PermissionResultCallback) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            when (status) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional -> {
                    cachedNotificationGranted = true
                    dispatch_async(dispatch_get_main_queue()) {
                        callback.onPermissionGranted()
                    }
                }
                UNAuthorizationStatusDenied -> {
                    cachedNotificationGranted = false
                    dispatch_async(dispatch_get_main_queue()) {
                        callback.onPermissionDenied(isPermanentDenied = true)
                    }
                }
                UNAuthorizationStatusNotDetermined, null -> {
                    val options = UNAuthorizationOptionAlert or
                        UNAuthorizationOptionSound or
                        UNAuthorizationOptionBadge
                    center.requestAuthorizationWithOptions(options) { granted, _ ->
                        cachedNotificationGranted = granted
                        dispatch_async(dispatch_get_main_queue()) {
                            if (granted) {
                                callback.onPermissionGranted()
                            } else {
                                callback.onPermissionDenied(isPermanentDenied = true)
                            }
                        }
                    }
                }
                else -> {
                    cachedNotificationGranted = false
                    dispatch_async(dispatch_get_main_queue()) {
                        callback.onPermissionDenied(isPermanentDenied = false)
                    }
                }
            }
        }
    }

    override fun isNotificationPermissionGranted(): Boolean {
        return cachedNotificationGranted
    }

    override fun checkNotificationPermission(callback: (Boolean) -> Unit) {
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            val granted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                settings?.authorizationStatus == UNAuthorizationStatusProvisional
            cachedNotificationGranted = granted
            dispatch_async(dispatch_get_main_queue()) {
                callback(granted)
            }
        }
    }
}
