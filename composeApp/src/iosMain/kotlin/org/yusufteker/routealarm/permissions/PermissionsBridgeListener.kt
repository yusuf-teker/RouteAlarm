package org.yusufteker.routealarm.permissions

actual interface PermissionsBridgeListener {
    actual fun requestLocationPermission(callback: PermissionResultCallback)
    actual fun requestBackgroundLocationPermission(callback: PermissionResultCallback)
    actual fun isLocationPermissionGranted(): Boolean

    actual fun requestNotificationPermission(callback: PermissionResultCallback)

    actual fun isNotificationPermissionGranted(): Boolean

}