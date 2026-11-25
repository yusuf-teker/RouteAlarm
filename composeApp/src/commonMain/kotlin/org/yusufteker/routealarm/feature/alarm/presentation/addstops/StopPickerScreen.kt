package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.core.presentation.button.PrimaryButton
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.presentation.addstops.components.TransportTypeChipRow
import org.yusufteker.routealarm.feature.location.presentation.LocationSearchBar
import org.yusufteker.routealarm.feature.location.presentation.PlatformMap
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.add_stop
import routealarm.composeapp.generated.resources.current_location
import routealarm.composeapp.generated.resources.stop_title

@Composable
fun StopPickerScreenRoot(
    viewModel: StopPickerViewModel = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    onBackClick: () -> Unit,
    navigateToAddAlarm: (Stop) -> Unit
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> onBackClick()
                is UiEvent.NavigateWithData<*> -> {
                    if (event.route == Routes.AddAlarmScreen) {
                        navigateToAddAlarm(event.data as Stop)
                    }
                }
                else -> Unit
            }
        }
    }

    StopPickerScreen(
        state = state.value,
        onAction = viewModel::onAction,
        contentPadding = contentPadding
    )
}

@Composable
fun StopPickerScreen(
    state: StopPickerState,
    onAction: (StopPickerAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val bottomPadding = systemBarsPadding.calculateBottomPadding()
    val topPadding = systemBarsPadding.calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        PlatformMap(
            currentLocation = state.currentLocation,
            modifier = Modifier.fillMaxSize(),
            selectedLocation = state.location,
            onLocationSelected = { selectedLocation ->
                onAction(StopPickerAction.LocationSelected(selectedLocation))
            },
            onCenterLocationConsumed = {
                onAction(StopPickerAction.OnCenterMapLocationConsumed)
            },
            centerToCurrentLocation = state.centerToCurrentLocation,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding + 8.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocationSearchBar(
                modifier = Modifier.padding(horizontal = 12.dp),
                query = state.query,
                onQueryChanged = { query -> onAction(StopPickerAction.QueryChanged(query)) },
                suggestions = state.suggestions,
                onSuggestionClicked = { suggestion ->
                    onAction(StopPickerAction.SuggestionSelected(suggestion))
                    keyboardController?.hide()
                },
                currentLocation = state.currentLocation,
                isSearching = state.isSearching,
                hasSearched = state.hasSearched,
                onBackClick = { onAction(StopPickerAction.NavigateBack) }
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { onAction(StopPickerAction.CenterMapOnCurrentLocation) },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.cardBackground.copy(alpha = 0.94f))
                    .border(1.dp, AppColors.border, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = stringResource(Res.string.current_location),
                    tint = AppColors.accent
                )
            }

            AnimatedVisibility(
                visible = state.showBottom,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 320)
                ) + fadeIn(animationSpec = tween(320))
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = AppColors.cardBackground.copy(alpha = 0.98f),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = AppColors.border,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                            .padding(bottom = bottomPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = state.stop.name,
                            onValueChange = { onAction(StopPickerAction.TitleChanged(it)) },
                            label = { Text(stringResource(Res.string.stop_title)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AppColors.surfaceElevated,
                                unfocusedContainerColor = AppColors.surfaceElevated,
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

                        TransportTypeChipRow(
                            selected = state.selectedTransportType,
                            onSelected = { type ->
                                onAction(StopPickerAction.SelectTransportType(type))
                            }
                        )

                        PrimaryButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(Res.string.add_stop),
                            onClick = { onAction(StopPickerAction.AddStop(state.stop)) }
                        )

                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}
