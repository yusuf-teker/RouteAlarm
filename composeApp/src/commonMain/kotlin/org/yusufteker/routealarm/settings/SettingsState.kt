package org.yusufteker.routealarm.settings

data class SettingsState(
    val stopProximityThresholdMeters: Int = 250,
    val isVibrationEnabled: Boolean = true,
    val isTestSoundPlaying: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val hasBackgroundLocationPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false
)
