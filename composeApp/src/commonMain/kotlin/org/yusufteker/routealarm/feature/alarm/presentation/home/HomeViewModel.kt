package org.yusufteker.routealarm.feature.alarm.presentation.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.feature.alarm.domain.AlarmActivationHandler
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.permissions.NotificationPermissionDialog
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.permissions.PermissionResultCallback
import org.yusufteker.routealarm.permissions.openAppSettings

class HomeViewModel(
    private val alarmRepository: AlarmRepository,
    private val permissionBridge: PermissionBridge,
    private val alarmActivationHandler: AlarmActivationHandler

) : BaseViewModel() {

    private var observeJob: Job? = null

    private val _uiState = MutableStateFlow(
        HomeState()
    )
    val uiState: StateFlow<HomeState> = _uiState.onStart {
        observeAlarms()
        requestNotificationPermission()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value)

    private fun observeAlarms() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {

            alarmRepository.getAlarmsWithStops2().collect { alarms ->
                val active = alarms.find { it.isActive }
                active?.stops?.forEach { stop ->
                    println("Stop ${stop.id}: isPassed = ${stop.isPassed}")
                }
                _uiState.update {
                    it.copy(
                        alarms = alarms, activeAlarm = active
                    )
                }
            }

        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnAlarmCheckedChange -> {
                alarmActivationHandler.handleAlarmToggle(
                    alarm = action.alarm,
                    isChecked = action.isChecked,
                    currentAlarms = uiState.value.alarms,
                    permissionBridge = permissionBridge,
                    scope = viewModelScope,
                )

            }

            is HomeAction.OnAlarmClick -> {

            }

            is HomeAction.OnDeleteAlarm -> {
                viewModelScope.launch {
                    alarmRepository.deleteAlarm(action.alarmId)
                }
            }

            HomeAction.OnAddAlarmClick -> {
                navigateTo(Routes.AddAlarmScreen)
            }
        }
    }

    private fun requestNotificationPermission() {
        permissionBridge.requestNotificationPermission(callback = object :
            PermissionResultCallback {
            override fun onPermissionGranted() {

            }

            override fun onPermissionDenied(isPermanentDenied: Boolean) {
                popupManager.showCustom(
                    content = { onDismiss ->
                        NotificationPermissionDialog(onDismiss = {
                            onDismiss()
                        }, onContinueClicked = {
                            openAppSettings()
                        })
                    })

            }
        })
    }
}

