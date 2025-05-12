package org.yusufteker.routealarm.permissions

import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

class IOSLocationPermissionsHandler : PermissionsBridgeListener {

    private val locationManager = CLLocationManager()

    override fun requestLocationPermission(callback: PermissionResultCallback) {
        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> {
                callback.onPermissionGranted()
                println("Konum izni: verildi") // Bazen görünür, bazen görünmez.
            }

            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                callback.onPermissionDenied(isPermanentDenied = true)
                println("Konum izni: verilmedi") // Bazen görünür, bazen görünmez.

            }

            kCLAuthorizationStatusNotDetermined -> {
                // Kullanıcıdan ilk kez izin isteniyor
                // Bu çağrı native sistem popup’ını gösterir
                locationManager.requestWhenInUseAuthorization()
                println("requestWhenInUseAuthorization") // Bazen görünür, bazen görünmez.

                // Not: Bu çağrının sonucu callback ile dönmez, yetki değişimini delegate üzerinden izlemek gerekir
                //callback.onPermissionDenied(isPermanentDenied = false) // Şimdilik belirsiz, false döneriz
            }

            else -> {
                callback.onPermissionDenied(isPermanentDenied = false)
            }
        }
    }

    override fun isLocationPermissionGranted(): Boolean {
        return when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> true
            else -> false
        }
    }
}