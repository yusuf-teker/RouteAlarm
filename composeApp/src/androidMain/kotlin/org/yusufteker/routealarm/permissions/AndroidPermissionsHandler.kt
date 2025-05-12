package org.yusufteker.routealarm.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat


class AndroidLocationPermissionsHandler(
    private val activity: ComponentActivity,
    permissionBridge: PermissionBridge
) : PermissionsBridgeListener {

    init {
        permissionBridge.setListener(this)
    }

    private var permissionResultCallback: PermissionResultCallback? = null

    private val requestLocationPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                permissionResultCallback?.onPermissionGranted()
            } else {
                val permanentlyDenied =
                    !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                permissionResultCallback?.onPermissionDenied(permanentlyDenied)
            }
        }

    override fun requestLocationPermission(callback: PermissionResultCallback) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        when {
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED -> {
                callback.onPermissionGranted()
            }

            activity.shouldShowRequestPermissionRationale(permission) -> {
                // Rationale gösterilecek. Bu durumda callback ile ViewModel'e false gönderiyoruz.
                callback.onPermissionDenied(false)
            }

            else -> {
                permissionResultCallback = callback
                requestLocationPermissionLauncher.launch(permission)
            }
        }
    }

    override fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestBackgroundPermission(callback: PermissionResultCallback) {
        permissionResultCallback = callback
        requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    fun requestBackgroundLocationPermission(callback: PermissionResultCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
            val backgroundLocationPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

            when {
                // 1. Önce Fine Location verilmiş mi?
                ContextCompat.checkSelfPermission(activity, fineLocationPermission) != PackageManager.PERMISSION_GRANTED -> {
                    // Fine yoksa önce onu iste
                    permissionResultCallback = object : PermissionResultCallback {
                        override fun onPermissionGranted() {
                            // Fine verildi, şimdi background iznini iste
                            requestBackgroundPermission(callback)
                        }

                        override fun onPermissionDenied(isPermanentDenied: Boolean) {
                            callback.onPermissionDenied(isPermanentDenied)
                        }
                    }
                    requestLocationPermissionLauncher.launch(fineLocationPermission)
                }

                // 2. Fine varsa, Background verilmiş mi?
                ContextCompat.checkSelfPermission(activity, backgroundLocationPermission) == PackageManager.PERMISSION_GRANTED -> {
                    callback.onPermissionGranted()
                }

                // 3. Background izni verilmemişse ve gerekçesi gösterilebilir mi?
                activity.shouldShowRequestPermissionRationale(backgroundLocationPermission) -> {
                    callback.onPermissionDenied(false)
                }

                // 4. Background iste
                else -> {
                    permissionResultCallback = callback
                    requestBackgroundPermissionLauncher.launch(backgroundLocationPermission)
                }
            }
        } else {
            // Android 9 ve öncesi için gerekmez
            callback.onPermissionGranted()
        }
    }

    private val requestBackgroundPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                permissionResultCallback?.onPermissionGranted()
            } else {
                val permanentlyDenied =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                            !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

                permissionResultCallback?.onPermissionDenied(permanentlyDenied)
            }
        }
}