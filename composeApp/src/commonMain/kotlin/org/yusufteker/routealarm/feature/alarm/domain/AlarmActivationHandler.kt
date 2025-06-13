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

class AlarmActivationHandler(
    private val alarmRepository: AlarmRepository,
    private val locationTracker: LocationTracker,
    private val locationService: LocationService,
    private val settingsManager: SettingsManager,
    private val popupManager: PopupManager,
) {
    fun handleAlarmToggle(
        alarm: Alarm,
        isChecked: Boolean,
        currentAlarms: List<Alarm>,
        permissionBridge: PermissionBridge,
        scope: CoroutineScope,
        onAlarmActivationSuccess: () -> Unit = {},
        onAlarmDeactivationSuccess: () -> Unit = {}

    ) {
        permissionBridge.requestBackgroundLocationPermission(object : PermissionResultCallback {
            override fun onPermissionGranted() {
                scope.launch {
                    val alreadyActive = currentAlarms.any { it.isActive }
                    val tryingToActivate = isChecked

                    if (alreadyActive && tryingToActivate) {
                        popupManager.showPopup(PopupType.Info("Hata", "Sadece bir alarm aktif olabilir"))
                    } else {
                        alarmRepository.setAlarmActive(alarm.id, isChecked)

                        if (isChecked) {
                            locationTracker.startTracking(alarm.id)
                            val startingLocation = locationService.getCurrentLocation()
                            settingsManager.setStartLocation(
                                alarm.stops.first().name,
                                startingLocation?.lat,
                                startingLocation?.lng
                            )
                            onAlarmActivationSuccess.invoke()
                        } else {
                            alarmRepository.setAllStopIsPassed(false)
                            locationTracker.stopTracking()
                            onAlarmDeactivationSuccess.invoke()
                        }
                    }
                }
            }

            override fun onPermissionDenied(isPermanentDenied: Boolean) {
                var currentPopup: PopupType.Custom? = null

                currentPopup = PopupType.Custom(
                    content = { _ -> // dismissCallback parametresini kullanmayın
                        LocationPermissionDialog(
                            onDismiss = {
                                currentPopup?.let { popupManager.dismissPopup(it) }
                            },
                            onContinueClicked = {
                                currentPopup?.let { popupManager.dismissPopup(it) }
                                openAppSettings()
                            }
                        )
                    },
                    onDismiss = {} // Boş bırakın
                )

                popupManager.showPopup(currentPopup)
            }
        })
    }
}
