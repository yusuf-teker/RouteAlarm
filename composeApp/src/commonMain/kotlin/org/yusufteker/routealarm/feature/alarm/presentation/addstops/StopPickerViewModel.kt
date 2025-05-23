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

    class StopPickerViewModel(
        private val placeSuggestionService: PlaceSuggestionService,
        private val locationService: LocationService

    ) : BaseViewModel() {


        private var searchJob: Job? = null

        private val _state = MutableStateFlow(StopPickerState())

        val state: StateFlow<StopPickerState> = _state.asStateFlow()

        private val _state2 = MutableStateFlow("Hello")
        val state2: StateFlow<String> = _state2

        fun updateMessage(newMsg: String) {
            _state2.value = newMsg
        }

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
                        showBottom = true
                    )
                    clearQueryAndSuggestions()
                }

                is StopPickerAction.AddStop -> {
                    val currentStop = _state.value.stop
                    if (currentStop.name.isBlank()) {

                        showErrorPopup("Durak adı boş olamaz")

                        return
                    } else {
                        sendUiEventSafe(UiEvent.NavigateWithData(Routes.AddAlarmScreen, currentStop))
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
                    val changeStopName = _state.value.stop.name.ifBlank { action.suggestionPlace.name }
                    _state.value = _state.value.copy(
                        query = action.suggestionPlace.name,
                        suggestions = emptyList(),
                        stop = _state.value.stop.copy(name = changeStopName),
                        showBottom = true
                    )

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
