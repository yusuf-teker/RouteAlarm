package org.yusufteker.routealarm.feature.alarm.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.feature.alarm.domain.Stop

//todo singleton yap
class SharedAlarmViewModel : BaseViewModel() {
    private val _stops = MutableStateFlow<List<Stop>>(emptyList())
    val stops: StateFlow<List<Stop>> = _stops.asStateFlow()

    fun addStop(stop: Stop) {
        _stops.value = _stops.value + stop
    }

    fun clearStops() {
        _stops.value = emptyList()
    }
}