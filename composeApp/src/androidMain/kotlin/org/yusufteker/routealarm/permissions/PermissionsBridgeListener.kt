package org.yusufteker.routealarm.permissions

actual interface PermissionsBridgeListener {
    actual fun requestLocationPermission(callback: PermissionResultCallback)
    actual fun isLocationPermissionGranted(): Boolean
}