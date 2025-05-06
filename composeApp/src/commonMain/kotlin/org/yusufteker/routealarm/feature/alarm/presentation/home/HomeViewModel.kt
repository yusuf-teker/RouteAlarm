package org.yusufteker.routealarm.feature.alarm.presentation.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository

class HomeViewModel(
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private var observeJob: Job? = null

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState .onStart {
        //observeAlarms()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value)


}
