package org.yusufteker.routealarm.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class AndroidLocationPermissionsHandler(
    private val activity: ComponentActivity,
    permissionBridge: PermissionBridge
) : PermissionsBridgeListener {

    private var locationCallback: PermissionResultCallback? = null
    private var backgroundCallback: PermissionResultCallback? = null
    private var notificationCallback: PermissionResultCallback? = null

    private val requestLocationPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            val cb = locationCallback
            locationCallback = null

            if (fineGranted || coarseGranted) {
                cb?.onPermissionGranted()
            } else {
                val permanentlyDenied =
                    !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                cb?.onPermissionDenied(permanentlyDenied)
            }
        }

    private val requestBackgroundPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val cb = backgroundCallback
            backgroundCallback = null
            if (isGranted) {
                cb?.onPermissionGranted()
            } else {
                val permanentlyDenied =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                cb?.onPermissionDenied(permanentlyDenied)
            }
        }

    private val requestNotificationPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val cb = notificationCallback
            notificationCallback = null
            if (isGranted) {
                cb?.onPermissionGranted()
            } else {
                val permanentlyDenied =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                cb?.onPermissionDenied(permanentlyDenied)
            }
        }

    init {
        permissionBridge.setListener(this)
    }

    override fun requestLocationPermission(callback: PermissionResultCallback) {
        if (isLocationPermissionGranted()) {
            callback.onPermissionGranted()
            return
        }
        locationCallback = callback
        requestLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun isLocationPermissionGranted(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    override fun isBackgroundLocationPermissionGranted(): Boolean {
        if (!isLocationPermissionGranted()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestBackgroundLocationPermission(callback: PermissionResultCallback) {
        if (!isLocationPermissionGranted()) {
            requestLocationPermission(object : PermissionResultCallback {
                override fun onPermissionGranted() {
                    requestBackgroundLocationPermission(callback)
                }

                override fun onPermissionDenied(isPermanentDenied: Boolean) {
                    callback.onPermissionDenied(isPermanentDenied)
                }
            })
            return
        }

        if (isBackgroundLocationPermissionGranted()) {
            callback.onPermissionGranted()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundCallback = callback
            requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            callback.onPermissionGranted()
        }
    }

    override fun requestNotificationPermission(callback: PermissionResultCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isNotificationPermissionGranted()) {
                callback.onPermissionGranted()
            } else {
                notificationCallback = callback
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            callback.onPermissionGranted()
        }
    }

    override fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun checkNotificationPermission(callback: (Boolean) -> Unit) {
        callback(isNotificationPermissionGranted())
    }
}
