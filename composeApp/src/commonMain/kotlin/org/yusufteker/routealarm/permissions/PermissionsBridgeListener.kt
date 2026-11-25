package org.yusufteker.routealarm.permissions

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

expect interface PermissionsBridgeListener {
    fun requestLocationPermission(callback: PermissionResultCallback)
    fun requestBackgroundLocationPermission(callback: PermissionResultCallback)
    fun isLocationPermissionGranted(): Boolean
    fun isBackgroundLocationPermissionGranted(): Boolean
    fun requestNotificationPermission(callback: PermissionResultCallback)
    fun isNotificationPermissionGranted(): Boolean
    fun checkNotificationPermission(callback: (Boolean) -> Unit)
}

class PermissionBridge {

    private var listener: PermissionsBridgeListener? = null

    private val _appResumed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val appResumed: SharedFlow<Unit> = _appResumed.asSharedFlow()

    fun setListener(listener: PermissionsBridgeListener) {
        this.listener = listener
    }

    fun notifyAppResumed() {
        _appResumed.tryEmit(Unit)
    }

    fun requestLocationPermission(callback: PermissionResultCallback) {
        listener?.requestLocationPermission(callback) ?: callback.onPermissionDenied(false)
    }

    fun requestBackgroundLocationPermission(callback: PermissionResultCallback) {
        listener?.requestBackgroundLocationPermission(callback) ?: callback.onPermissionDenied(false)
    }

    fun isLocationPermissionGranted(): Boolean {
        return listener?.isLocationPermissionGranted() ?: false
    }

    fun isBackgroundLocationPermissionGranted(): Boolean {
        return listener?.isBackgroundLocationPermissionGranted() ?: false
    }

    fun requestNotificationPermission(callback: PermissionResultCallback) {
        listener?.requestNotificationPermission(callback) ?: callback.onPermissionDenied(false)
    }

    fun isNotificationPermissionGranted(): Boolean {
        return listener?.isNotificationPermissionGranted() ?: false
    }

    fun checkNotificationPermission(callback: (Boolean) -> Unit) {
        listener?.checkNotificationPermission(callback) ?: callback(false)
    }
}

interface PermissionResultCallback {
    fun onPermissionGranted()
    fun onPermissionDenied(isPermanentDenied: Boolean)
}
