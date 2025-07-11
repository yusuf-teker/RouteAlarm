package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.core.presentation.UiText
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.permissions.PermissionResultCallback
import org.yusufteker.routealarm.permissions.openAppSettings
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.alarm_save_failed
import routealarm.composeapp.generated.resources.enter_title_and_stop
import routealarm.composeapp.generated.resources.error
import routealarm.composeapp.generated.resources.location_permission_required
import routealarm.composeapp.generated.resources.permission_denied
import routealarm.composeapp.generated.resources.permission_permanently_denied
import routealarm.composeapp.generated.resources.permission_required

class AddAlarmViewModel(
    private val repository: AlarmRepository,
    private val permissionBridge: PermissionBridge,
) : BaseViewModel() {

    private val _state = MutableStateFlow(AddAlarmState())
    val state: StateFlow<AddAlarmState> = _state

    fun onAction(action: AddAlarmAction) {
        when (action) {
            is AddAlarmAction.TitleChanged -> {
                _state.value = _state.value.copy(title = action.newTitle)
            }

            is AddAlarmAction.RemoveStop -> {
                val updatedStops = _state.value.stops - action.stop
                _state.value = _state.value.copy(stops = updatedStops)
            }

            is AddAlarmAction.SaveAlarm -> {
                saveAlarm()
            }

            is AddAlarmAction.OnStopsChange -> {
                val updatedStops = action.stops.mapIndexed { index, stop ->
                    stop.copy(orderIndex = index + 1) // stop.orderIndex’i index ile güncelle
                }
                _state.value = _state.value.copy(
                    stops = updatedStops
                )
            }
            is AddAlarmAction.CheckLocationPermission -> checkLocationPermission()
            is AddAlarmAction.RequestLocationPermission -> requestLocationPermission()
        }
    }

    private fun saveAlarm() {
        val currentState = _state.value
        if (currentState.title.isBlank() || currentState.stops.isEmpty()) {
            popupManager.showError(message = Res.string.enter_title_and_stop)
            return
        }

        viewModelScope.launch {
            try {
                repository.saveAlarmWithStops(
                    Alarm(
                        title = currentState.title,
                        isActive = false,
                    ),
                    stops = currentState.stops

                )
                sendUiEvent(UiEvent.NavigateTo(Routes.HomeScreen))
                _state.value = AddAlarmState() // reset state
            } catch (e: Exception) {
                popupManager.showError( message =Res.string.alarm_save_failed)
            } finally {

            }

        }


    }
    private fun requestLocationPermission() {
        _state.value = _state.value.copy(isLoading = true)

        permissionBridge.requestLocationPermission(object : PermissionResultCallback {
            override fun onPermissionGranted() {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isLocationPermissionGranted = true,
                )
                sendUiEventSafe(UiEvent.NavigateTo(Routes.StopPickerScreen))

            }

            override fun onPermissionDenied(isPermanentDenied: Boolean) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = if (isPermanentDenied) Res.string.permission_permanently_denied else Res.string.permission_denied
                )
                popupManager.showConfirm(
                    title = Res.string.permission_required,
                    message = Res.string.location_permission_required,
                    onConfirm = {
                        openAppSettings()
                    }
                )
            }
        })
    }

    private fun checkLocationPermission() {
        val isLocationPermissionGranted = permissionBridge.isLocationPermissionGranted()
        _state.value = _state.value.copy(
            isLocationPermissionGranted = isLocationPermissionGranted
        )
        if (isLocationPermissionGranted){
            sendUiEventSafe(UiEvent.NavigateTo(Routes.StopPickerScreen))
        }else{
            requestLocationPermission()
        }

    }

}