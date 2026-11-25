package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.core.presentation.button.PrimaryButton
import org.yusufteker.routealarm.feature.alarm.presentation.SharedAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.AddStopCard
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.StopCard
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.add_alarm
import routealarm.composeapp.generated.resources.add_first_stop
import routealarm.composeapp.generated.resources.alarm_title
import routealarm.composeapp.generated.resources.no_stops_added
import routealarm.composeapp.generated.resources.route_stops
import routealarm.composeapp.generated.resources.save
import routealarm.composeapp.generated.resources.stop_count
import routealarm.composeapp.generated.resources.vibration
import routealarm.composeapp.generated.resources.vibration_desc

@Composable
fun AddAlarmScreenRoot(
    viewModel: AddAlarmViewModel = koinViewModel(),
    sharedViewModel: SharedAlarmViewModel = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    navigateToStopPicker: () -> Unit,
    navigateToHome: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateTo -> {
                    when (event.route) {
                        Routes.StopPickerScreen -> navigateToStopPicker()
                        Routes.HomeScreen -> navigateToHome()
                        else -> Unit
                    }
                }
                else -> Unit
            }
        }
    }

    AddAlarmScreen(
        state = state,
        onAction = { action ->
            if (action is AddAlarmAction.RemoveStop) {
                sharedViewModel.removeStopAt(action.index)
            }
            viewModel.onAction(action)
        },
        contentPadding = contentPadding
    )
}

@Composable
fun AddAlarmScreen(
    state: AddAlarmState,
    onAction: (AddAlarmAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .statusBarsPadding()
            .padding(bottom = contentPadding.calculateBottomPadding())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.add_alarm),
            style = AppTypography.headlineLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = { onAction(AddAlarmAction.TitleChanged(it)) },
            label = { Text(stringResource(Res.string.alarm_title)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.cardBackground,
                unfocusedContainerColor = AppColors.cardBackground,
                focusedBorderColor = AppColors.accent,
                unfocusedBorderColor = AppColors.border,
                focusedTextColor = AppColors.textPrimary,
                unfocusedTextColor = AppColors.textPrimary,
                focusedLabelColor = AppColors.accentSoft,
                unfocusedLabelColor = AppColors.textSecondary,
                cursorColor = AppColors.accent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.cardBackground,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AppColors.accentSoft,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.vibration),
                        style = AppTypography.titleMedium.copy(fontSize = 15.sp)
                    )
                    Text(
                        text = stringResource(Res.string.vibration_desc),
                        style = AppTypography.labelSmall.copy(color = AppColors.textSecondary)
                    )
                }
                Switch(
                    checked = state.isVibration,
                    onCheckedChange = { onAction(AddAlarmAction.ToggleVibration(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.onAccent,
                        checkedTrackColor = AppColors.accent,
                        uncheckedThumbColor = AppColors.textSecondary,
                        uncheckedTrackColor = AppColors.cardBackgroundSecondary,
                        uncheckedBorderColor = AppColors.border
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.route_stops),
                style = AppTypography.titleMedium.copy(fontSize = 17.sp)
            )
            Text(
                text = stringResource(Res.string.stop_count, state.stops.size),
                style = AppTypography.labelSmall.copy(color = AppColors.accentSoft)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        AddStopCard(
            onClick = { onAction(AddAlarmAction.CheckLocationPermission) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            if (state.stops.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AppColors.cardBackground.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = AppColors.textSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = stringResource(Res.string.no_stops_added),
                                style = AppTypography.titleMedium.copy(
                                    color = AppColors.textPrimary,
                                    fontSize = 15.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(Res.string.add_first_stop),
                                style = AppTypography.labelSmall.copy(color = AppColors.textSecondary),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = state.stops,
                    key = { index, stop -> "${index}_${stop.name}_${stop.latitude}_${stop.longitude}" }
                ) { index, stop ->
                    StopCard(
                        stop = stop,
                        index = index,
                        onDeleteStop = { onAction(AddAlarmAction.RemoveStop(index)) }
                    )
                }
            }
        }

        PrimaryButton(
            text = stringResource(Res.string.save),
            onClick = { onAction(AddAlarmAction.SaveAlarm) },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}
