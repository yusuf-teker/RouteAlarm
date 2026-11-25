package org.yusufteker.routealarm.feature.alarm.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.feature.alarm.domain.Stop

class SharedAlarmViewModel : BaseViewModel() {
    private val _stops = MutableStateFlow<List<Stop>>(emptyList())
    val stops: StateFlow<List<Stop>> = _stops.asStateFlow()

    fun addStop(stop: Stop) {
        val nextIndex = _stops.value.size + 1
        _stops.value = _stops.value + stop.copy(orderIndex = nextIndex)
    }

    fun removeStopAt(index: Int) {
        if (index !in _stops.value.indices) return
        _stops.value = _stops.value
            .filterIndexed { i, _ -> i != index }
            .mapIndexed { i, stop -> stop.copy(orderIndex = i + 1) }
    }

    fun clearStops() {
        _stops.value = emptyList()
    }
}
