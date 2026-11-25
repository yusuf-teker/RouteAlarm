package org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail

import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.feature.alarm.data.mappers.toLocation
import org.yusufteker.routealarm.feature.alarm.domain.AlarmActivationHandler
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.alarm.domain.calculateActiveStopProgress
import org.yusufteker.routealarm.feature.location.data.LocationService
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.LocationTracker
import org.yusufteker.routealarm.feature.location.domain.calculateDistance
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.settings.SettingsManager

class AlarmDetailViewModel(
    private val alarmRepository: AlarmRepository,
    private val settingsManager: SettingsManager,
    private val locationService: LocationService,
    private val permissionBridge: PermissionBridge,
    private val alarmActivationHandler: AlarmActivationHandler,
    private val locationTracker: LocationTracker
) : BaseViewModel() {

    private val _state = MutableStateFlow(AlarmDetailState())
    val state: StateFlow<AlarmDetailState> = _state.asStateFlow()

    private var observeAlarmsJob: Job? = null
    private var locationJob: Job? = null
    private var currentAlarmId: Int? = null

    fun onAction(action: AlarmDetailAction) {
        when (action) {
            is AlarmDetailAction.LoadData -> {
                if (currentAlarmId != action.alarmId || observeAlarmsJob?.isActive != true) {
                    currentAlarmId = action.alarmId
                    observeAlarm(action.alarmId)
                }
            }

            is AlarmDetailAction.OnAlarmCheckedChange -> {
                alarmActivationHandler.handleAlarmToggle(
                    alarm = action.alarm,
                    isChecked = action.isChecked,
                    currentAlarms = state.value.alarms,
                    permissionBridge = permissionBridge,
                    scope = viewModelScope,
                    onAlarmActivationSuccess = {
                        _state.update { it.copy(isAlarmActive = true) }
                        startLocationUpdates()
                    },
                    onAlarmDeactivationSuccess = {
                        _state.update { it.copy(isAlarmActive = false) }
                        stopLocationUpdates()
                    }
                )
            }

            is AlarmDetailAction.OnDeleteAlarm -> {
                viewModelScope.launch {
                    val isDeletingActive = _state.value.alarm?.id == action.alarmId && _state.value.isAlarmActive
                    stopLocationUpdates()
                    if (isDeletingActive) {
                        locationTracker.stopTracking()
                    }
                    alarmRepository.deleteAlarm(action.alarmId)
                    sendUiEventSafe(UiEvent.NavigateBack)
                }
            }

            is AlarmDetailAction.NavigateBack -> {
                sendUiEventSafe(UiEvent.NavigateBack)
            }
        }
    }

    private fun observeAlarm(id: Int) {
        observeAlarmsJob?.cancel()
        observeAlarmsJob = viewModelScope.launch {
            alarmRepository.getAlarmsWithStops2().collect { alarms ->
                val rawAlarm = alarms.find { it.id == id }
                val sortedStops = rawAlarm?.stops?.sortedBy { it.orderIndex }.orEmpty()
                val alarm = rawAlarm?.copy(stops = sortedStops)
                val startTriple = settingsManager.startLocation.first()
                val startLoc = startTriple?.let { (name, lat, lng) ->
                    Location(name = name, lat = lat, lng = lng)
                }

                val nextStop = sortedStops.firstOrNull { !it.isPassed }
                val nextIndex = if (nextStop != null) sortedStops.indexOf(nextStop) else sortedStops.size
                val previousStops = sortedStops.take(nextIndex)
                val upcomingStops = if (nextStop != null) sortedStops.drop(nextIndex + 1) else emptyList()

                _state.update {
                    it.copy(
                        alarms = alarms,
                        alarm = alarm,
                        isAlarmActive = alarm?.isActive ?: false,
                        startLocation = startLoc,
                        activeStop = nextStop,
                        previousStops = previousStops,
                        nextStops = upcomingStops
                    )
                }

                updateProgress()

                if (alarm?.isActive == true) {
                    if (locationJob?.isActive != true) {
                        startLocationUpdates()
                    }
                } else {
                    stopLocationUpdates()
                }
            }
        }
    }

    private fun updateProgress() {
        val currentState = _state.value
        val alarm = currentState.alarm ?: return
        val currentLoc = currentState.currentLocation ?: return
        val progressInfo = calculateActiveStopProgress(alarm, currentState.startLocation, currentLoc) ?: return

        _state.update {
            it.copy(
                progress = progressInfo.progress,
                remainingDistance = progressInfo.remainingDistanceMeters,
                currentDistanceToNextStop = progressInfo.remainingDistanceMeters.toDouble()
            )
        }
    }

    fun startLocationUpdates() {
        if (locationJob?.isActive == true) return
        _state.update { it.copy(isLoading = true) }

        locationJob = viewModelScope.launch {
            while (isActive) {
                val currentLocation = try {
                    locationService.getCurrentLocation()
                } catch (e: Exception) {
                    Napier.e("getCurrentLocation failed: ${e.message}", tag = "AlarmDetailVM")
                    null
                }

                val alarm = _state.value.alarm
                if (currentLocation == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLocationNotFound = true
                        )
                    }
                } else {
                    val sortedStops = alarm?.stops?.sortedBy { it.orderIndex }.orEmpty()
                    val nextStop = sortedStops.firstOrNull { !it.isPassed }
                    val nextIndex = if (nextStop != null) sortedStops.indexOf(nextStop) else sortedStops.size

                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLocationNotFound = false,
                            currentLocation = currentLocation,
                            activeStop = nextStop,
                            previousStops = sortedStops.take(nextIndex),
                            nextStops = if (nextStop != null) sortedStops.drop(nextIndex + 1) else emptyList()
                        )
                    }
                    updateProgress()
                }

                delay(2000)
            }
        }
    }

    fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
        _state.update { it.copy(isLoading = false) }
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        observeAlarmsJob?.cancel()
    }
}
