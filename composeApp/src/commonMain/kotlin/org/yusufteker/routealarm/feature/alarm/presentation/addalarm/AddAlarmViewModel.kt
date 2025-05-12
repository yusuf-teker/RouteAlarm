package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.permissions.PermissionResultCallback
import org.yusufteker.routealarm.permissions.openAppSettings

class AddAlarmViewModel(
    private val repository: AlarmRepository,
    private val permissionBridge: PermissionBridge
) : BaseViewModel() {

    private val _state = MutableStateFlow(AddAlarmState())
    val state: StateFlow<AddAlarmState> = _state

    fun onAction(action: AddAlarmAction) {
        when (action) {
            is AddAlarmAction.TitleChanged -> {
                _state.value = _state.value.copy(title = action.newTitle)
            }

            is AddAlarmAction.AddStop -> {

            }

            is AddAlarmAction.RemoveStop -> {
                val updatedStops = _state.value.stops - action.stop
                _state.value = _state.value.copy(stops = updatedStops)
            }

            is AddAlarmAction.SaveAlarm -> {
                saveAlarm()
            }

            is AddAlarmAction.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }

            is AddAlarmAction.OnStopsChange -> {

                _state.value = _state.value.copy(
                    stops = action.stops
                )
            }
            is AddAlarmAction.CheckLocationPermission -> checkLocationPermission()
            is AddAlarmAction.RequestLocationPermission -> requestLocationPermission()
        }
    }

    private fun saveAlarm() {
        val currentState = _state.value
        if (currentState.title.isBlank() || currentState.stops.isEmpty()) {
            _state.value = currentState.copy(errorMessage = "Başlık ve en az bir durak girin.")
            //showErrorPopup("Başlık ve en az bir durak girin.")
            //_state.value = currentState.copy(canAddAndNavigate = false)
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isSaving = true)
            try {
                repository.saveAlarmWithStops(
                    Alarm(
                        id = 0, // Auto-ID or dummy
                        title = currentState.title,
                        isActive = false,
                    ),
                    stops = currentState.stops

                )
                //_state.value = _state.value.copy(canAddAndNavigate = true)
                _state.value = AddAlarmState() // reset state
            } catch (e: Exception) {
                _state.value =
                    currentState.copy(isSaving = false, errorMessage = "Alarm kaydedilemedi.")
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
                    canNavigateStopPicker = true
                )

            }

            override fun onPermissionDenied(isPermanentDenied: Boolean) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = if (isPermanentDenied) "Permission permanently denied" else "Permission denied"
                )
                showConfirmPopup(
                    title = "Permission Required",
                    message = "Location permission is required to add stops. Please grant permission in settings.",
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
            _state.value = _state.value.copy(
                canNavigateStopPicker = true
            )
        }else{
            requestLocationPermission()
        }

    }

    fun clearNavigateState(){ // todo sonradan düşünülecek
        _state.value = _state.value.copy(canNavigateStopPicker = false)
    }

}