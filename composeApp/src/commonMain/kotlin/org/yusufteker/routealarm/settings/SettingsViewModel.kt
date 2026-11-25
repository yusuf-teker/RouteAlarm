package org.yusufteker.routealarm.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.permissions.PermissionResultCallback
import org.yusufteker.routealarm.permissions.openAppSettings

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val permissionBridge: PermissionBridge,
    private val alarmSoundPlayer: AlarmSoundPlayer
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private var soundMonitorJob: Job? = null

    init {
        viewModelScope.launch {
            settingsManager.stopProximityThresholdMeters.collectLatest { meters ->
                _state.update { it.copy(stopProximityThresholdMeters = meters) }
            }
        }
        viewModelScope.launch {
            settingsManager.isVibrationEnabled.collectLatest { enabled ->
                _state.update { it.copy(isVibrationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            permissionBridge.appResumed.collect {
                refreshPermissions()
            }
        }
        refreshPermissions()
    }

    fun refreshPermissions() {
        val locationGranted = permissionBridge.isLocationPermissionGranted()
        val backgroundGranted = permissionBridge.isBackgroundLocationPermissionGranted()
        _state.update {
            it.copy(
                hasLocationPermission = locationGranted,
                hasBackgroundLocationPermission = backgroundGranted,
                hasNotificationPermission = permissionBridge.isNotificationPermissionGranted()
            )
        }
        permissionBridge.checkNotificationPermission { notifGranted ->
            _state.update { it.copy(hasNotificationPermission = notifGranted) }
        }
    }

    fun onThresholdChange(meters: Int) {
        viewModelScope.launch {
            settingsManager.setStopProximityThresholdMeters(meters)
        }
    }

    fun onVibrationChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setVibrationEnabled(enabled)
        }
    }

    fun onToggleTestSound() {
        if (alarmSoundPlayer.isPlaying()) {
            stopTestSound()
        } else {
            alarmSoundPlayer.play(withVibration = _state.value.isVibrationEnabled)
            _state.update { it.copy(isTestSoundPlaying = true) }
            monitorTestSound()
        }
    }

    private fun monitorTestSound() {
        soundMonitorJob?.cancel()
        soundMonitorJob = viewModelScope.launch {
            while (isActive && alarmSoundPlayer.isPlaying()) {
                delay(400)
            }
            _state.update { it.copy(isTestSoundPlaying = false) }
        }
    }

    private fun stopTestSound() {
        soundMonitorJob?.cancel()
        alarmSoundPlayer.stop()
        _state.update { it.copy(isTestSoundPlaying = false) }
    }

    fun requestLocationPermission() {
        permissionBridge.requestLocationPermission(object : PermissionResultCallback {
            override fun onPermissionGranted() = refreshPermissions()
            override fun onPermissionDenied(isPermanentDenied: Boolean) {
                refreshPermissions()
                if (isPermanentDenied) openAppSettings()
            }
        })
    }

    fun requestBackgroundLocationPermission() {
        permissionBridge.requestBackgroundLocationPermission(object : PermissionResultCallback {
            override fun onPermissionGranted() = refreshPermissions()
            override fun onPermissionDenied(isPermanentDenied: Boolean) {
                refreshPermissions()
                openAppSettings()
            }
        })
    }

    fun requestNotificationPermission() {
        permissionBridge.requestNotificationPermission(object : PermissionResultCallback {
            override fun onPermissionGranted() = refreshPermissions()
            override fun onPermissionDenied(isPermanentDenied: Boolean) {
                refreshPermissions()
                if (isPermanentDenied) openAppSettings()
            }
        })
    }

    fun onOpenAppSettings() {
        openAppSettings()
    }

    override fun onCleared() {
        super.onCleared()
        soundMonitorJob?.cancel()
        if (alarmSoundPlayer.isPlaying()) {
            alarmSoundPlayer.stop()
        }
    }
}
