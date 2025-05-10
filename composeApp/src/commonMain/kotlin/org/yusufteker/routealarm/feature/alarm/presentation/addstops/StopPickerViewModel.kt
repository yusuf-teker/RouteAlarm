package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.core.presentation.popup.PopupType

class StopPickerViewModel() : BaseViewModel() {

    private val _state = MutableStateFlow(StopPickerState())
    val state: StateFlow<StopPickerState> = _state.asStateFlow()

    fun onAction(action: StopPickerAction) {
        when (action) {
            is StopPickerAction.QueryChanged -> {
                val query = action.value
                _state.value = _state.value.copy(query = query)
            }

            is StopPickerAction.LocationSelected -> {
                // Todo Location yapisi eklenecek
            }

            is StopPickerAction.AddStop -> {
                val currentStop = _state.value.stop
                if (currentStop.name.isBlank()) {
                    popupManager.showPopup(
                        PopupType.Error("Durak adı boş olamaz")
                    )
                    _state.value = _state.value.copy(canAddAndNavigate = false)
                    return
                }else{
                    _state.value = _state.value.copy(canAddAndNavigate = true)
                }
                //NavHostta shared view modele eklendi

            }

            is StopPickerAction.SelectTransportType -> {
                _state.value = _state.value.copy(selectedTransportType = action.type)
            }

            is StopPickerAction.TitleChanged -> {
                _state.value =
                    _state.value.copy(stop = _state.value.stop.copy(name = action.newTitle))
            }
        }
    }


}
