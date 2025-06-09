package org.yusufteker.routealarm.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {


    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    init {
        viewModelScope.launch {
            settingsManager.stopProximityThresholdMeters.collectLatest { meters ->
                _state.value = _state.value.copy(stopProximityThresholdMeters = meters)
            }
        }
    }

    fun onThresholdChange(meters: Int) {
        viewModelScope.launch {
            settingsManager.setStopProximityThresholdMeters(meters)
        }
    }
}