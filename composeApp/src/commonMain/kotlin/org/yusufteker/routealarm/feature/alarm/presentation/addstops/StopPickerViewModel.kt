    package org.yusufteker.routealarm.feature.alarm.presentation.addstops

    import androidx.lifecycle.viewModelScope
    import kotlinx.coroutines.Job
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.launch
    import org.yusufteker.routealarm.app.Routes
    import org.yusufteker.routealarm.core.presentation.BaseViewModel
    import org.yusufteker.routealarm.core.presentation.UiEvent
    import org.yusufteker.routealarm.feature.alarm.domain.addPlace
    import org.yusufteker.routealarm.feature.location.data.LocationService
    import org.yusufteker.routealarm.feature.location.data.PlaceSuggestionService
    import org.yusufteker.routealarm.feature.location.domain.emptyLocation
    import org.yusufteker.routealarm.feature.location.domain.toLocation
    import routealarm.composeapp.generated.resources.Res
    import routealarm.composeapp.generated.resources.stop_name_cannot_be_empty

    class StopPickerViewModel(
        private val placeSuggestionService: PlaceSuggestionService,
        private val locationService: LocationService

    ) : BaseViewModel() {


        private var searchJob: Job? = null

        private val _state = MutableStateFlow(StopPickerState())

        val state: StateFlow<StopPickerState> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                val location = locationService.getCurrentLocation()
                _state.value = _state.value.copy(
                    currentLocation = location ?: emptyLocation
                )
            }
        }

        fun onAction(action: StopPickerAction) {
            when (action) {
                is StopPickerAction.QueryChanged -> {
                    val query = action.value
                    _state.value = _state.value.copy(query = query)
                    searchPlaces(query)
                }

                is StopPickerAction.LocationSelected -> {
                    _state.value = _state.value.copy(
                        location = action.location,
                        stop = _state.value.stop.copy(
                            latitude = action.location.lat,
                            longitude = action.location.lng

                        ),
                        showBottom = true
                    )
                    clearQueryAndSuggestions()
                }

                is StopPickerAction.AddStop -> {
                    val currentStop = _state.value.stop
                    if (currentStop.name.isBlank()) {

                        popupManager.showError(Res.string.stop_name_cannot_be_empty)

                        return
                    } else {
                        sendUiEventSafe(UiEvent.NavigateWithData(Routes.AddAlarmScreen, currentStop))
                    }


                }

                is StopPickerAction.SelectTransportType -> {
                    _state.value = _state.value.copy(
                        stop = _state.value.stop.copy(transportType = action.type),
                        selectedTransportType = action.type
                    )
                }

                is StopPickerAction.TitleChanged -> {
                    _state.value =
                        _state.value.copy(stop = _state.value.stop.copy(name = action.newTitle))
                }

                is StopPickerAction.SuggestionSelected -> {
                    _state.value = _state.value.copy(selectedPlace = null)

                    val changeStopName = _state.value.stop.name.ifBlank { action.suggestionPlace.name }

                    _state.value = _state.value.copy(
                        query = action.suggestionPlace.name,
                        suggestions = emptyList(),
                        showBottom = true,
                        location = action.suggestionPlace.toLocation(),
                        stop = _state.value.stop.addPlace(action.suggestionPlace.copy(name = changeStopName))
                    )

                    //Android için lat lng gelmiyor bu yüzden detay servisi lazım
                    if (action.suggestionPlace.longitude == emptyLocation.lng && action.suggestionPlace.latitude == emptyLocation.lat){
                        selectPlace(placeId = action.suggestionPlace.id)

                    }

                }

                StopPickerAction.NavigateBack -> {
                    sendUiEventSafe(UiEvent.NavigateBack)
                }

                StopPickerAction.CenterMapOnCurrentLocation -> {
                    viewModelScope.launch {
                        val location = locationService.getCurrentLocation()
                        _state.value = _state.value.copy(
                            currentLocation = location ?: emptyLocation,
                            centerToCurrentLocation = true
                        )
                    }
                }

                StopPickerAction.OnCenterMapLocationConsumed -> {
                    _state.value = _state.value.copy(centerToCurrentLocation = false)
                }
            }

        }


        fun searchPlaces(query: String) {
            searchJob?.cancel()

            searchJob = viewModelScope.launch {
                delay(300)

                if (query.isNotEmpty()) {
                    val results = placeSuggestionService.getSuggestions(query)
                    _state.value = _state.value.copy(suggestions = results)
                } else {
                    _state.value = _state.value.copy(suggestions = emptyList())
                }
            }
        }

        // Bir yer seçildiğinde, yerin detaylarını al
        fun selectPlace(placeId: String) {
            viewModelScope.launch {
                val place = placeSuggestionService.getPlaceDetails(placeId)
                _state.value = _state.value.copy(
                    location = place?.toLocation() ?: emptyLocation,
                    stop = _state.value.stop.addPlace(place)
                )

            }
        }

        fun clearQueryAndSuggestions(){
            _state.value = _state.value.copy(
                query = "",
                suggestions = emptyList(),
            )
        }

    }
