package org.yusufteker.routealarm.feature.alarm.presentation.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmActivationHandler
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.alarm.domain.calculateActiveStopProgress
import org.yusufteker.routealarm.feature.location.data.LocationService
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.LocationTracker
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.permissions.PermissionResultCallback
import org.yusufteker.routealarm.settings.SettingsManager

class HomeViewModel(
    private val alarmRepository: AlarmRepository,
    private val permissionBridge: PermissionBridge,
    private val alarmActivationHandler: AlarmActivationHandler,
    private val locationTracker: LocationTracker,
    private val settingsManager: SettingsManager,
    private val locationService: LocationService
) : BaseViewModel() {

    private var observeJob: Job? = null
    private var locationProgressJob: Job? = null
    private var hasRequestedNotificationPermission = false
    private var lastKnownLocation: Location? = null
    private var startLocation: Location? = null

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
        observeAlarms()
        requestNotificationPermissionOnce()
    }

    private fun observeAlarms() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            alarmRepository.getAlarmsWithStops2().collect { alarms ->
                val active = alarms.find { it.isActive }
                startLocation = settingsManager.startLocation.first()?.let { (name, lat, lng) ->
                    Location(name = name, lat = lat, lng = lng)
                }

                val progressInfo = active?.let {
                    calculateActiveStopProgress(it, startLocation, lastKnownLocation)
                }
                _uiState.update {
                    it.copy(
                        alarms = alarms,
                        activeAlarm = active,
                        activeStopIndex = progressInfo?.activeStopIndex
                            ?: active?.stops?.sortedBy { s -> s.orderIndex }?.indexOfFirst { s -> !s.isPassed }
                            ?: -1,
                        activeStopProgress = progressInfo?.progress ?: 0f,
                        remainingDistanceMeters = progressInfo?.remainingDistanceMeters?.toDouble(),
                        isLoading = false
                    )
                }

                if (active != null) {
                    startProgressTracking(active)
                } else {
                    stopProgressTracking()
                }
            }
        }
    }

    private fun startProgressTracking(activeAlarm: Alarm) {
        if (locationProgressJob?.isActive == true) return
        locationProgressJob = viewModelScope.launch {
            while (isActive) {
                val current = try {
                    locationService.getCurrentLocation()
                } catch (_: Exception) {
                    null
                }
                if (current != null) {
                    lastKnownLocation = current
                    val latestActive = _uiState.value.activeAlarm ?: activeAlarm
                    val currentStart = settingsManager.startLocation.first()?.let { (name, lat, lng) ->
                        Location(name = name, lat = lat, lng = lng)
                    } ?: startLocation
                    startLocation = currentStart
                    val progressInfo = calculateActiveStopProgress(latestActive, currentStart, current)
                    if (progressInfo != null) {
                        _uiState.update {
                            it.copy(
                                activeStopIndex = progressInfo.activeStopIndex,
                                activeStopProgress = progressInfo.progress,
                                remainingDistanceMeters = progressInfo.remainingDistanceMeters.toDouble()
                            )
                        }
                    }
                }
                delay(4000)
            }
        }
    }

    private fun stopProgressTracking() {
        locationProgressJob?.cancel()
        locationProgressJob = null
        lastKnownLocation = null
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

            is HomeAction.OnAlarmClick -> Unit

            is HomeAction.OnDeleteAlarm -> {
                viewModelScope.launch {
                    val isDeletingActive = _uiState.value.activeAlarm?.id == action.alarmId
                    if (isDeletingActive) {
                        locationTracker.stopTracking()
                    }
                    alarmRepository.deleteAlarm(action.alarmId)
                }
            }

            HomeAction.OnAddAlarmClick -> {
                navigateTo(Routes.AddAlarmScreen)
            }
        }
    }

    private fun requestNotificationPermissionOnce() {
        if (hasRequestedNotificationPermission) return
        hasRequestedNotificationPermission = true
        permissionBridge.checkNotificationPermission { granted ->
            if (!granted) {
                permissionBridge.requestNotificationPermission(object : PermissionResultCallback {
                    override fun onPermissionGranted() = Unit
                    override fun onPermissionDenied(isPermanentDenied: Boolean) = Unit
                })
            }
        }
    }
}
