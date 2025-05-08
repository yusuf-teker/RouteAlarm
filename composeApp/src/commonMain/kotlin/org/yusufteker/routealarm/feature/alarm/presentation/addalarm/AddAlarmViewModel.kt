package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository

class AddAlarmViewModel(
    private val repository: AlarmRepository
) : ViewModel() {

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
        }
    }

    private fun saveAlarm() {
        val currentState = _state.value
        if (currentState.title.isBlank() || currentState.stops.isEmpty()) {
            _state.value = currentState.copy(errorMessage = "Başlık ve en az bir durak girin.")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isSaving = true)
            try {
                repository.addAlarm(
                    Alarm(
                        id = 0, // Auto-ID or dummy
                        title = currentState.title,
                        stops = currentState.stops
                    )
                )
                _state.value = AddAlarmState() // reset state
            } catch (e: Exception) {
                _state.value = currentState.copy(isSaving = false, errorMessage = "Alarm kaydedilemedi.")
            }
        }
    }
}