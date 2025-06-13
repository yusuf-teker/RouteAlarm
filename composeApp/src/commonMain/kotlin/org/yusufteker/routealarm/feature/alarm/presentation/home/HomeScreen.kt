package org.yusufteker.routealarm.feature.alarm.presentation.home

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.data.dummy.fakeAlarms
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.ActiveAlarm
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.ActiveAlarmMax2Stop
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.AlarmList
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.EmptyHomeScreen

@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToAlarmDetail: (Int) -> Unit,
    navigateToAddAlarm: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event){

                is UiEvent.NavigateTo -> {
                    if (event.route == Routes.AddAlarmScreen){
                        navigateToAddAlarm()
                    }
                }
                else -> Unit
            }
        }
    }


    HomeScreen(
        state = state, onAction = { action ->
            when (action) {
                //Diğer ekranlarla ilgili olanlar bu kısımda diğerleri viewmodelde
                is HomeAction.OnAlarmClick -> onNavigateToAlarmDetail(action.alarmId)
                else -> Unit
            }
            viewModel.onAction(action)
        }, contentPadding = contentPadding
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {

    Column(
        modifier = Modifier.fillMaxSize().background(
            AppColors.background
        ).padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (state.alarms.isNotEmpty()){
            AlarmList(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                alarms = state.alarms,
                onAlarmCheckedChange = { alarm, isChecked ->
                    onAction(HomeAction.OnAlarmCheckedChange(alarm, isChecked))
                },
                onDeleteAlarm = { alarm ->
                    onAction(HomeAction.OnDeleteAlarm(alarm.id))
                },
                onAlarmClick = { alarmId ->
                    onAction(HomeAction.OnAlarmClick(alarmId = alarmId))
                }
            )
        }else{
            EmptyHomeScreen { onAction(HomeAction.OnAddAlarmClick) }
        }

        if (state.activeAlarm != null) {
            if (state.activeAlarm.stops.size>2){
                ActiveAlarm(state.activeAlarm )
            }else{
                ActiveAlarmMax2Stop(alarm = state.activeAlarm )
            }
        }
    }

}


