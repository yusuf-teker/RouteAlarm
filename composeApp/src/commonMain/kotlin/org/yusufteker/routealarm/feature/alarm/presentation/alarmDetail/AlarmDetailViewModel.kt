package org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail

import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.feature.alarm.data.mappers.toLocation
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmActivationHandler
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.location.data.LocationService
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.calculateDistance
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import org.yusufteker.routealarm.permissions.PermissionBridge
import org.yusufteker.routealarm.settings.SettingsManager


class AlarmDetailViewModel(
    private val alarmRepository: AlarmRepository,
    private val settingsManager: SettingsManager,
    private val locationService: LocationService,
    private val permissionBridge: PermissionBridge,
    private val alarmActivationHandler: AlarmActivationHandler

) : BaseViewModel() {

    private val _state = MutableStateFlow(AlarmDetailState())
    val state: StateFlow<AlarmDetailState> = _state

    private var observeJob: Job? = null

    fun onAction(action: AlarmDetailAction) {
        when (action) {
            is AlarmDetailAction.LoadData -> {
                viewModelScope.launch {
                    observeAlarm(action.alarmId)

                }
            }

            is AlarmDetailAction.OnAlarmCheckedChange -> {
                alarmActivationHandler.handleAlarmToggle(
                    alarm = action.alarm,
                    isChecked = action.isChecked,
                    currentAlarms = listOf(action.alarm),
                    permissionBridge = permissionBridge,
                    scope = viewModelScope,
                    onAlarmActivationSuccess = {
                        _state.update { it.copy(isAlarmActive =  action.isChecked) }
                        startLocationUpdates()

                    },
                    onAlarmDeactivationSuccess = {
                        _state.update { it.copy(isAlarmActive = action.isChecked) }
                        stopLocationUpdates()
                    }
                )
            }
            is AlarmDetailAction.OnDeleteAlarm -> {
                viewModelScope.launch {
                    alarmRepository.deleteAlarm(action.alarmId)
                }
            }
            is AlarmDetailAction.NavigateBack -> {
                sendUiEventSafe(UiEvent.NavigateBack)
            }

        }
    }

    fun observeAlarm(id: Int) {
        Napier.d("observeAlarm started for id=$id", tag = "Yusuf")
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            alarmRepository.getAlarmWithStopsByIdFlow(id).collect { alarm ->
                Napier.d("observeAlarm collected new alarm: $alarm", tag = "Yusuf")
                val startLocationSM = settingsManager.startLocation

                _state.value = _state.value.copy(
                    alarm = alarm,
                    isAlarmActive = alarm?.isActive == true,
                    startLocation = Location(
                        name = startLocationSM.first()?.first ?: "",
                        lat = startLocationSM.first()?.second ?: -1.0,
                        lng = startLocationSM.first()?.third ?: -1.0
                    ),
                )
                updateProgress()
                if (state.value.isAlarmActive){
                    startLocationUpdates()
                }
            }



            Napier.d("startLocationUpdates called from observeAlarm", tag = "Yusuf")
        }


    }

    suspend fun getAlarm(alarmId: Int): Alarm? {
        Napier.d("getAlarmDetail called with id=$alarmId", tag = "Yusuf")
        return alarmRepository.getAlarmByIdWithStops(id = alarmId)
    }

    private fun updateProgress() {
        val state = _state.value
        val alarm = state.alarm
        if (alarm == null) {
            Napier.d("Alarm is null, returning from updateProgress", tag = "Yusuf")
            return
        }

        val currentLat = state.currentLocation?.lat ?: -1.0
        val currentLng = state.currentLocation?.lng ?: -1.0
        Napier.d("Current location: lat=$currentLat, lng=$currentLng", tag = "Yusuf")

        val stops = alarm.stops.sortedBy { it.orderIndex }.filter { !it.isPassed }

        if (stops.isEmpty()) {
            return
        }

        val nextStop = stops.first()


        val isFirstStop = alarm.stops.first() == nextStop
        Napier.d("Next stop: $nextStop, isFirstStop=$isFirstStop", tag = "Yusuf")

        val previousStop: Location? = if (isFirstStop)
            state.startLocation
        else
            alarm.stops[alarm.stops.indexOf(nextStop) - 1].toLocation()

        Napier.d("Previous stop: $previousStop", tag = "Yusuf")

        previousStop?.let {
            val totalDistance = calculateDistance(
                previousStop.lat,
                previousStop.lng,
                nextStop.latitude,
                nextStop.longitude
            ).toFloat()

            val remaining = calculateDistance(
                currentLat,
                currentLng,
                nextStop.latitude,
                nextStop.longitude
            ).toFloat()

            val progress = ((1f - (remaining / totalDistance)).coerceIn(0f, 1f))
            Napier.d("totalDistance=$totalDistance, remaining=$remaining, progress=$progress", tag = "Yusuf")

            _state.value = _state.value.copy(
                progress = progress,
                remainingDistance = remaining
            )
        }
        Napier.d ("setLoading false update", tag = "Loading")
    }

    private var locationJob: Job? = null

    fun startLocationUpdates() {
        Napier.d ("setLoading true start ", tag = "Loading")
        _state.update { it.copy(isLoading = true) }


        locationJob?.cancel()
        locationJob = viewModelScope.launch {

            while (isActive) {
                val alarm = _state.value.alarm

                val currentLocation = locationService.getCurrentLocation()

                if (alarm != null && currentLocation != null) {
                    val nextStop = alarm.stops.firstOrNull { !it.isPassed }

                    if (nextStop != null){ // Mevcut stop öncesindekiler ve sonrasındakiler
                        val nextStopIndex = alarm.stops.indexOf(nextStop)
                        _state.update {
                            it.copy(
                                previousStops = alarm.stops.take(nextStopIndex),
                                nextStops = alarm.stops.drop(nextStopIndex + 1)
                            )
                        }
                    }

                    Napier.d("Next stop for distance calculation: $nextStop", tag = "Yusuf")
                    if (nextStop != state.value.activeStop) {
                        Napier.d("Active stop changed to: $nextStop", tag = "Yusuf")
                        _state.update { it.copy(activeStop = nextStop) }
                    }
                    if (nextStop != null) {
                        val distance = calculateDistance(
                            currentLocation.lat,
                            currentLocation.lng,
                            nextStop.latitude,
                            nextStop.longitude
                        )
                        Napier.d("Calculated distance to next stop: ${formatDistance(distance)}", tag = "Yusuf")

                        _state.update {
                            it.copy(
                                currentDistanceToNextStop = distance,
                                currentLocation = currentLocation
                            )
                        }
                    }

                    _state.update { it.copy(isLoading = false) }
                } else {
                    if (alarm == null) Napier.d("Alarm is null in location update loop", tag = "Yusuf")
                    if (currentLocation == null) Napier.d("currentLocation is null in location update loop", tag = "Yusuf")
                }
                Napier.d("*******************************************************************************************")

                updateProgress()
                delay(1000L)
            }

        }
    }

    fun stopLocationUpdates() {
        Napier.d("stopLocationUpdates called", tag = "Loading")
        Napier.d ("setLoading false stop ", tag = "Loading")
        _state.update { it.copy(isLoading = false) }

        locationJob?.cancel()
    }


}
