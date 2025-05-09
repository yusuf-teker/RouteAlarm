package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StopPickerViewModel(

) : ViewModel() {

    private val _state = MutableStateFlow(StopPickerState())
    val state: StateFlow<StopPickerState> = _state.asStateFlow()

    fun onAction(action: StopPickerAction) {
        when (action) {
            is StopPickerAction.QueryChanged -> {
                val query = action.value

                _state.value = _state.value.copy(query = query, )
            }

            is StopPickerAction.LocationSelected -> {
                // Todo Location yapisi eklenecek
            }

            is StopPickerAction.AddStop -> {
                //NavHostta shared view modele eklendi

            }
        }
    }


}
