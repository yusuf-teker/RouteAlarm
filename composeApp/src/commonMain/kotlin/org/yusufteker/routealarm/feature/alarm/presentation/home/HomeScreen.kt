package org.yusufteker.routealarm.feature.alarm.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.ActiveAlarm
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.AlarmList
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.EmptyHomeScreen
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.alarms

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
            when (event) {
                is UiEvent.NavigateTo -> {
                    if (event.route == Routes.AddAlarmScreen) {
                        navigateToAddAlarm()
                    }
                }
                else -> Unit
            }
        }
    }

    HomeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is HomeAction.OnAlarmClick -> onNavigateToAlarmDetail(action.alarmId)
                else -> Unit
            }
            viewModel.onAction(action)
        },
        contentPadding = contentPadding
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .statusBarsPadding()
            .padding(bottom = contentPadding.calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.alarms),
                style = AppTypography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        AnimatedVisibility(
            visible = state.activeAlarm != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            state.activeAlarm?.let { active ->
                ActiveAlarm(
                    alarm = active,
                    activeStopIndex = state.activeStopIndex,
                    activeStopProgress = state.activeStopProgress,
                    remainingDistanceMeters = state.remainingDistanceMeters,
                    onClick = { onAction(HomeAction.OnAlarmClick(active.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.isLoading && state.alarms.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            }

            state.alarms.isEmpty() -> {
                EmptyHomeScreen(
                    onAddAlarmClick = { onAction(HomeAction.OnAddAlarmClick) },
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                AlarmList(
                    alarms = state.alarms,
                    onCheckedChange = { alarm, isChecked ->
                        onAction(HomeAction.OnAlarmCheckedChange(alarm, isChecked))
                    },
                    onDeleteAlarm = { alarm ->
                        onAction(HomeAction.OnDeleteAlarm(alarm.id))
                    },
                    onAlarmClicked = { alarmId ->
                        onAction(HomeAction.OnAlarmClick(alarmId))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
