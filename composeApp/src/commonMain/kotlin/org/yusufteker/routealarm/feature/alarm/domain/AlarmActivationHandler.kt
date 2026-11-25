package org.yusufteker.routealarm.feature.alarm.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.core.presentation.popup.PopupManager
import org.yusufteker.routealarm.core.presentation.popup.PopupType
import org.yusufteker.routealarm.feature.location.data.LocationService
import org.yusufteker.routealarm.feature.location.domain.LocationTracker
import org.yusufteker.routealarm.permissions.LocationPermissionDialog
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.permissions.PermissionResultCallback
import org.yusufteker.routealarm.permissions.openAppSettings
import org.yusufteker.routealarm.settings.SettingsManager
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.enter_title_and_stop
import routealarm.composeapp.generated.resources.error
import routealarm.composeapp.generated.resources.only_one_alarm_active

class AlarmActivationHandler(
    private val alarmRepository: AlarmRepository,
    private val locationTracker: LocationTracker,
    private val locationService: LocationService,
    private val settingsManager: SettingsManager,
    private val popupManager: PopupManager,
) {
    private var backgroundPromptShownThisSession = false

    fun handleAlarmToggle(
        alarm: Alarm,
        isChecked: Boolean,
        currentAlarms: List<Alarm>,
        permissionBridge: PermissionBridge,
        scope: CoroutineScope,
        onAlarmActivationSuccess: () -> Unit = {},
        onAlarmDeactivationSuccess: () -> Unit = {}
    ) {
        if (!isChecked) {
            scope.launch {
                alarmRepository.setAlarmActive(alarm.id, false)
                alarmRepository.setStopsPassedForAlarm(alarm.id, false)
                locationTracker.stopTracking()
                onAlarmDeactivationSuccess.invoke()
            }
            return
        }

        if (alarm.stops.isEmpty()) {
            popupManager.showError(Res.string.enter_title_and_stop)
            return
        }

        val alreadyActive = currentAlarms.any { it.isActive && it.id != alarm.id }
        if (alreadyActive) {
            popupManager.showPopup(
                PopupType.Info(Res.string.error, Res.string.only_one_alarm_active)
            )
            return
        }

        // 1. Ensure foreground location permission first
        if (!permissionBridge.isLocationPermissionGranted()) {
            permissionBridge.requestLocationPermission(object : PermissionResultCallback {
                override fun onPermissionGranted() {
                    proceedAfterLocationGranted(
                        alarm = alarm,
                        permissionBridge = permissionBridge,
                        scope = scope,
                        onAlarmActivationSuccess = onAlarmActivationSuccess
                    )
                }

                override fun onPermissionDenied(isPermanentDenied: Boolean) {
                    showPermissionDialog(
                        canContinueWithForegroundOnly = false,
                        onContinueAnyway = {},
                        onOpenSettings = { openAppSettings() }
                    )
                }
            })
        } else {
            proceedAfterLocationGranted(
                alarm = alarm,
                permissionBridge = permissionBridge,
                scope = scope,
                onAlarmActivationSuccess = onAlarmActivationSuccess
            )
        }
    }

    private fun proceedAfterLocationGranted(
        alarm: Alarm,
        permissionBridge: PermissionBridge,
        scope: CoroutineScope,
        onAlarmActivationSuccess: () -> Unit
    ) {
        // Request notification permission if missing (non-blocking for activation)
        permissionBridge.checkNotificationPermission { notifGranted ->
            if (!notifGranted) {
                permissionBridge.requestNotificationPermission(object : PermissionResultCallback {
                    override fun onPermissionGranted() {}
                    override fun onPermissionDenied(isPermanentDenied: Boolean) {}
                })
            }
        }

        if (permissionBridge.isBackgroundLocationPermissionGranted() || backgroundPromptShownThisSession) {
            activateAlarm(alarm, scope, onAlarmActivationSuccess)
            return
        }

        backgroundPromptShownThisSession = true
        showPermissionDialog(
            canContinueWithForegroundOnly = true,
            onContinueAnyway = {
                activateAlarm(alarm, scope, onAlarmActivationSuccess)
            },
            onOpenSettings = {
                permissionBridge.requestBackgroundLocationPermission(object : PermissionResultCallback {
                    override fun onPermissionGranted() {
                        activateAlarm(alarm, scope, onAlarmActivationSuccess)
                    }

                    override fun onPermissionDenied(isPermanentDenied: Boolean) {
                        if (isPermanentDenied) {
                            openAppSettings()
                        } else if (permissionBridge.isLocationPermissionGranted()) {
                            activateAlarm(alarm, scope, onAlarmActivationSuccess)
                        }
                    }
                })
            }
        )
    }

    private fun activateAlarm(
        alarm: Alarm,
        scope: CoroutineScope,
        onAlarmActivationSuccess: () -> Unit
    ) {
        scope.launch {
            alarmRepository.setStopsPassedForAlarm(alarm.id, false)
            alarmRepository.setAlarmActive(alarm.id, true)
            locationTracker.startTracking(alarm.id)
            val startingLocation = runCatching { locationService.getCurrentLocation() }.getOrNull()
            val firstStopName = alarm.stops.minByOrNull { it.orderIndex }?.name ?: ""
            settingsManager.setStartLocation(
                firstStopName,
                startingLocation?.lat,
                startingLocation?.lng
            )
            onAlarmActivationSuccess.invoke()
        }
    }

    private fun showPermissionDialog(
        canContinueWithForegroundOnly: Boolean,
        onContinueAnyway: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        var currentPopup: PopupType.Custom? = null
        currentPopup = PopupType.Custom(
            content = { _ ->
                LocationPermissionDialog(
                    canContinueAnyway = canContinueWithForegroundOnly,
                    onDismiss = {
                        currentPopup?.let { popupManager.dismissPopup(it) }
                    },
                    onContinueClicked = {
                        currentPopup?.let { popupManager.dismissPopup(it) }
                        onOpenSettings()
                    },
                    onSkipClicked = {
                        currentPopup?.let { popupManager.dismissPopup(it) }
                        onContinueAnyway()
                    }
                )
            },
            onDismiss = {}
        )
        popupManager.showPopup(currentPopup)
    }
}
