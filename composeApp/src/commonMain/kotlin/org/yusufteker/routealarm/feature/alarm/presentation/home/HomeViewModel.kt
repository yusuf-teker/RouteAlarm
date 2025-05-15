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
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository

class HomeViewModel(
    private val alarmRepository: AlarmRepository
) : BaseViewModel() {

    private var observeJob: Job? = null

    private val _uiState = MutableStateFlow(
        HomeState()
    )
    val uiState: StateFlow<HomeState> = _uiState.onStart {
        addFakeAlarmsIfEmpty()
        observeAlarms()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value)

    private fun addFakeAlarmsIfEmpty() { // todo fake alarm kaldırılacak
        viewModelScope.launch {
            alarmRepository.getAlarms().collect { alarms ->
                if (alarms.isEmpty()) {
                    val fakeAlarms = listOf(
                        Alarm(
                            id = 1,
                            title = "Evden Okula",
                            isActive = false,
                            stops = listOf(/* dummy stops */)
                        ), Alarm(
                            id = 2,
                            title = "Okuldan Eve",
                            isActive = false,
                            stops = listOf(/* dummy stops */)
                        ), Alarm(
                            id = 3,
                            title = "Antrenmana Gidiş",
                            isActive = false,
                            stops = listOf(/* dummy stops */)
                        )
                    )
                    alarmRepository.insertAlarms(fakeAlarms)
                }
            }
        }
    }

    private fun observeAlarms() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {

            alarmRepository.getAlarmsWithStops().collect { alarms ->
                val active = alarms.find { it.isActive }

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
                viewModelScope.launch {
                    val alreadyActive = uiState.value.alarms.any { it.isActive }
                    val tryingToActivate = action.isChecked
                    if (alreadyActive && tryingToActivate) {
                        showInfoPopup("Hata","Sadece bir alarm aktif olamaz")
                    } else {
                        alarmRepository.setAlarmActive( action.alarm.id, action.isChecked)

                    }

                }
            }

            is HomeAction.OnAlarmClick -> {

            }

            is HomeAction.OnDeleteAlarm -> {
                viewModelScope.launch {
                    alarmRepository.deleteAlarm(action.alarmId)
                }
            }
        }
    }
}
