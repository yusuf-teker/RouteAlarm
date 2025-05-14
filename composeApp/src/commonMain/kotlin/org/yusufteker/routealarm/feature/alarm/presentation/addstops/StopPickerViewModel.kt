package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.core.presentation.BaseViewModel
import org.yusufteker.routealarm.feature.location.domain.PlaceSuggestionService
import org.yusufteker.routealarm.feature.location.domain.emptyLocation
import org.yusufteker.routealarm.feature.location.domain.toLocation

class StopPickerViewModel(
    private val placeSuggestionService: PlaceSuggestionService

) : BaseViewModel() {

    private val _state = MutableStateFlow(StopPickerState())
    val state: StateFlow<StopPickerState> = _state.asStateFlow()

    fun onAction(action: StopPickerAction) {
        when (action) {
            is StopPickerAction.QueryChanged -> {
                val query = action.value
                _state.value = _state.value.copy(query = query)
                searchPlaces(query)
            }

            is StopPickerAction.LocationSelected -> {
                _state.value = _state.value.copy(
                    location = action.location
                )
            }

            is StopPickerAction.AddStop -> {
                val currentStop = _state.value.stop
                if (currentStop.name.isBlank()) {

                    showErrorPopup("Durak adı boş olamaz")

                    _state.value = _state.value.copy(canAddAndNavigate = false)
                    return
                } else {
                    _state.value = _state.value.copy(canAddAndNavigate = true)
                }


            }

            is StopPickerAction.SelectTransportType -> {
                _state.value = _state.value.copy(selectedTransportType = action.type)
            }

            is StopPickerAction.TitleChanged -> {
                _state.value =
                    _state.value.copy(stop = _state.value.stop.copy(name = action.newTitle))
            }

            is StopPickerAction.SuggestionSelected -> {
                _state.value = _state.value.copy(selectedPlace = null)
                selectPlace(action.suggestionPlace.id)
                _state.value = _state.value.copy(
                    query = action.suggestionPlace.name,
                    suggestions = emptyList(),
                )

            }

            StopPickerAction.NavigateBack -> {
                // handled in the screen
            }
        }
    }


    fun searchPlaces(query: String) {
        viewModelScope.launch {
            val results = placeSuggestionService.getSuggestions(query)
            _state.value = _state.value.copy(suggestions = results)

        }
    }

    // Bir yer seçildiğinde, yerin detaylarını al
    fun selectPlace(placeId: String) {
        viewModelScope.launch {
            val placeDetails = placeSuggestionService.getPlaceDetails(placeId)
            _state.value = _state.value.copy(
                selectedPlace = placeDetails,
                location = placeDetails?.toLocation() ?: emptyLocation,
            )

        }
    }


}
