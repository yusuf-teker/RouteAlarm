package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.permissions.PermissionResultCallback
import org.yusufteker.routealarm.permissions.openAppSettings
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.alarm_save_failed
import routealarm.composeapp.generated.resources.enter_title_and_stop
import routealarm.composeapp.generated.resources.location_permission_required
import routealarm.composeapp.generated.resources.permission_denied
import routealarm.composeapp.generated.resources.permission_permanently_denied
import routealarm.composeapp.generated.resources.permission_required

class AddAlarmViewModel(
    private val repository: AlarmRepository,
    private val permissionBridge: PermissionBridge,
) : BaseViewModel() {

    private val _state = MutableStateFlow(AddAlarmState())
    val state: StateFlow<AddAlarmState> = _state.asStateFlow()

    fun onAction(action: AddAlarmAction) {
        when (action) {
            is AddAlarmAction.TitleChanged -> {
                _state.update { it.copy(title = action.newTitle) }
            }

            is AddAlarmAction.ToggleVibration -> {
                _state.update { it.copy(isVibration = action.enabled) }
            }

            is AddAlarmAction.RemoveStop -> {
                _state.update { current ->
                    val updatedStops = current.stops
                        .filterIndexed { index, _ -> index != action.index }
                        .mapIndexed { index, stop -> stop.copy(orderIndex = index + 1) }
                    current.copy(stops = updatedStops)
                }
            }

            is AddAlarmAction.SaveAlarm -> {
                saveAlarm()
            }

            is AddAlarmAction.OnStopsChange -> {
                val updatedStops = action.stops.mapIndexed { index, stop ->
                    stop.copy(orderIndex = index + 1)
                }
                _state.update { it.copy(stops = updatedStops) }
            }

            is AddAlarmAction.CheckLocationPermission -> checkLocationPermission()
            is AddAlarmAction.RequestLocationPermission -> requestLocationPermission()
        }
    }

    private fun saveAlarm() {
        val currentState = _state.value
        if (currentState.isLoading) return
        if (currentState.title.isBlank() || currentState.stops.isEmpty()) {
            popupManager.showError(message = Res.string.enter_title_and_stop)
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.saveAlarmWithStops(
                    alarm = Alarm(
                        title = currentState.title.trim(),
                        isActive = false,
                        isVibration = currentState.isVibration
                    ),
                    stops = currentState.stops
                )
                _state.value = AddAlarmState()
                sendUiEvent(UiEvent.NavigateTo(Routes.HomeScreen))
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                popupManager.showError(message = Res.string.alarm_save_failed)
            }
        }
    }

    private fun requestLocationPermission() {
        _state.update { it.copy(isLoading = true) }

        permissionBridge.requestLocationPermission(object : PermissionResultCallback {
            override fun onPermissionGranted() {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLocationPermissionGranted = true
                    )
                }
                sendUiEventSafe(UiEvent.NavigateTo(Routes.StopPickerScreen))
            }

            override fun onPermissionDenied(isPermanentDenied: Boolean) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = if (isPermanentDenied) {
                            Res.string.permission_permanently_denied
                        } else {
                            Res.string.permission_denied
                        }
                    )
                }
                popupManager.showConfirm(
                    title = Res.string.permission_required,
                    message = Res.string.location_permission_required,
                    onConfirm = { openAppSettings() }
                )
            }
        })
    }

    private fun checkLocationPermission() {
        val granted = permissionBridge.isLocationPermissionGranted()
        _state.update { it.copy(isLocationPermissionGranted = granted) }
        if (granted) {
            sendUiEventSafe(UiEvent.NavigateTo(Routes.StopPickerScreen))
        } else {
            requestLocationPermission()
        }
    }
}
