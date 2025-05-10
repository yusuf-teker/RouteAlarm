package org.yusufteker.routealarm.feature.alarm.presentation.home

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.data.dummy.fakeAlarms
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.ActiveAlarm
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.AlarmList

@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToAlarmDetail: (Alarm) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state, onAction = { action ->
            when (action) {
                //Diğer ekranlarla ilgili olanlar bu kısımda diğerleri viewmodelde
                is HomeAction.OnAlarmClick -> onNavigateToAlarmDetail(action.alarm)
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

        AlarmList(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
            alarms = state.alarms,
            onAlarmCheckedChange = { alarm, isChecked ->
                val action = HomeAction.OnAlarmCheckedChange(alarm, isChecked)
                onAction(action)
            },
            onDeleteAlarm = { alarm ->
                onAction(HomeAction.OnDeleteAlarm(alarm.id))
            })

        if (state.activeAlarm != null) {
            ActiveAlarm(fakeAlarms.first())
        }
    }

}


