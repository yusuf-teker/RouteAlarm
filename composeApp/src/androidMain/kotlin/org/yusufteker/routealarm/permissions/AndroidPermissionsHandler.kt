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

    override fun requestBackgroundLocationPermission(callback: PermissionResultCallback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 9 ve öncesi için gerekmez
            callback.onPermissionGranted()
            return
        }

        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val backgroundLocationPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

        when {
            // 1. Fine yoksa önce onu iste
            ContextCompat.checkSelfPermission(activity, fineLocationPermission) != PackageManager.PERMISSION_GRANTED -> {
                permissionResultCallback = object : PermissionResultCallback {
                    override fun onPermissionGranted() {
                        requestBackgroundLocationPermission(callback)
                    }

                    override fun onPermissionDenied(isPermanentDenied: Boolean) {
                        callback.onPermissionDenied(isPermanentDenied)
                    }
                }
                requestLocationPermissionLauncher.launch(fineLocationPermission)
            }

            // 2. Background zaten varsa
            ContextCompat.checkSelfPermission(activity, backgroundLocationPermission) == PackageManager.PERMISSION_GRANTED -> {
                callback.onPermissionGranted()
            }

            // 3. Android 10'da doğrudan istenebilir
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                requestBackgroundPermission(callback)
            }

            // 4. Android 11+ ise ayarlara yönlendirilmeli (çünkü ilk istek gösterilmiyor)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                callback.onPermissionDenied(false) // veya ViewModel'e "openSettings" gibi event gönder
            }

            else -> {
                callback.onPermissionDenied(false)
            }
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