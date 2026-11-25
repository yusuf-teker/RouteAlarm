package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
import routealarm.composeapp.generated.resources.error_occured
import routealarm.composeapp.generated.resources.stop_name_cannot_be_empty

class StopPickerViewModel(
    private val placeSuggestionService: PlaceSuggestionService,
    private val locationService: LocationService
) : BaseViewModel() {

    private var searchJob: Job? = null
    private var reverseGeocodeJob: Job? = null
    private var lastAutoTitle: String? = null

    private val _state = MutableStateFlow(StopPickerState())
    val state: StateFlow<StopPickerState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val location = locationService.getCurrentLocation()
            if (location != null) {
                _state.update { it.copy(currentLocation = location) }
            }
        }
    }

    fun onAction(action: StopPickerAction) {
        when (action) {
            is StopPickerAction.QueryChanged -> {
                val query = action.value
                _state.update {
                    it.copy(
                        query = query,
                        hasSearched = false
                    )
                }
                searchPlaces(query)
            }

            is StopPickerAction.LocationSelected -> {
                reverseGeocodeJob?.cancel()
                val tapped = action.location
                val resolvedTitle = tapped.name.trim()
                val canOverwriteTitle = _state.value.stop.name.isBlank() || _state.value.stop.name == lastAutoTitle
                if (resolvedTitle.isNotEmpty() && canOverwriteTitle) {
                    lastAutoTitle = resolvedTitle
                }

                _state.update { current ->
                    val nextTitle = if (resolvedTitle.isNotEmpty() && canOverwriteTitle) {
                        resolvedTitle
                    } else {
                        current.stop.name
                    }
                    current.copy(
                        location = tapped,
                        stop = current.stop.copy(
                            name = nextTitle,
                            latitude = tapped.lat,
                            longitude = tapped.lng
                        ),
                        showBottom = true,
                        query = "",
                        suggestions = emptyList(),
                        isSearching = false,
                        hasSearched = false
                    )
                }

                if (resolvedTitle.isEmpty()) {
                    resolveTappedLocationName(tapped.lat, tapped.lng)
                }
            }

            is StopPickerAction.AddStop -> {
                val currentStop = _state.value.stop
                when {
                    currentStop.name.isBlank() -> {
                        popupManager.showError(Res.string.stop_name_cannot_be_empty)
                    }
                    currentStop.latitude == emptyLocation.lat && currentStop.longitude == emptyLocation.lng -> {
                        popupManager.showError(Res.string.error_occured)
                    }
                    else -> {
                        val cleanStop = currentStop.copy(name = currentStop.name.trim())
                        sendUiEventSafe(UiEvent.NavigateWithData(Routes.AddAlarmScreen, cleanStop))
                    }
                }
            }

            is StopPickerAction.SelectTransportType -> {
                _state.update { current ->
                    current.copy(
                        stop = current.stop.copy(transportType = action.type),
                        selectedTransportType = action.type
                    )
                }
            }

            is StopPickerAction.TitleChanged -> {
                if (action.newTitle != lastAutoTitle) {
                    lastAutoTitle = null
                }
                _state.update { current ->
                    current.copy(stop = current.stop.copy(name = action.newTitle))
                }
            }

            is StopPickerAction.SuggestionSelected -> {
                reverseGeocodeJob?.cancel()
                val selected = action.suggestionPlace
                val canOverwriteTitle = _state.value.stop.name.isBlank() || _state.value.stop.name == lastAutoTitle
                val preserveOrPlaceName = if (canOverwriteTitle) {
                    lastAutoTitle = selected.name
                    selected.name
                } else {
                    _state.value.stop.name
                }
                val hasCoords = selected.latitude != emptyLocation.lat || selected.longitude != emptyLocation.lng

                _state.update { current ->
                    current.copy(
                        selectedPlace = selected,
                        query = selected.name,
                        suggestions = emptyList(),
                        isSearching = false,
                        hasSearched = false,
                        showBottom = true,
                        location = if (hasCoords) selected.toLocation() else current.location,
                        stop = current.stop.addPlace(selected.copy(name = preserveOrPlaceName))
                    )
                }

                if (!hasCoords) {
                    selectPlace(placeId = selected.id)
                }
            }

            StopPickerAction.NavigateBack -> {
                sendUiEventSafe(UiEvent.NavigateBack)
            }

            StopPickerAction.CenterMapOnCurrentLocation -> {
                viewModelScope.launch {
                    val location = locationService.getCurrentLocation()
                    if (location != null) {
                        _state.update {
                            it.copy(
                                currentLocation = location,
                                centerToCurrentLocation = true
                            )
                        }
                    }
                }
            }

            StopPickerAction.OnCenterMapLocationConsumed -> {
                _state.update { it.copy(centerToCurrentLocation = false) }
            }
        }
    }

    private fun searchPlaces(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update {
                it.copy(
                    suggestions = emptyList(),
                    isSearching = false,
                    hasSearched = false
                )
            }
            return
        }

        _state.update { it.copy(isSearching = true) }
        searchJob = viewModelScope.launch {
            delay(300)
            val nearLocation = _state.value.currentLocation.takeIf { it != emptyLocation }
            val results = placeSuggestionService.getSuggestions(query.trim(), nearLocation)
            _state.update {
                it.copy(
                    suggestions = results,
                    isSearching = false,
                    hasSearched = true
                )
            }
        }
    }

    private fun resolveTappedLocationName(lat: Double, lng: Double) {
        reverseGeocodeJob = viewModelScope.launch {
            val resolvedName = placeSuggestionService.reverseGeocode(lat, lng) ?: return@launch
            _state.update { current ->
                if (current.location.lat != lat || current.location.lng != lng) {
                    return@update current
                }
                val canFillTitle = current.stop.name.isBlank() || current.stop.name == lastAutoTitle
                val updatedTitle = if (canFillTitle) {
                    lastAutoTitle = resolvedName
                    resolvedName
                } else {
                    current.stop.name
                }
                current.copy(
                    location = current.location.copy(name = resolvedName),
                    stop = current.stop.copy(name = updatedTitle)
                )
            }
        }
    }

    private fun selectPlace(placeId: String) {
        viewModelScope.launch {
            val place = placeSuggestionService.getPlaceDetails(placeId) ?: return@launch
            _state.update { current ->
                val resolvedName = current.stop.name.ifBlank { place.name }
                current.copy(
                    location = place.toLocation(),
                    stop = current.stop.addPlace(place.copy(name = resolvedName))
                )
            }
        }
    }
}
