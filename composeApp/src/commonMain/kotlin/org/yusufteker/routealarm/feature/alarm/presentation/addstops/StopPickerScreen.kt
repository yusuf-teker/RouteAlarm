package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.core.presentation.UiText
import org.yusufteker.routealarm.core.presentation.button.PrimaryButton
import org.yusufteker.routealarm.core.presentation.card.AdaptiveCard
import org.yusufteker.routealarm.core.presentation.card.CardContent
import org.yusufteker.routealarm.core.presentation.card.LayoutOrientation
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.presentation.SharedAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addstops.components.TransportTypeDropdownSelector
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
            when(event){
                is UiEvent.NavigateBack -> {
                    onBackClick()
                }
                is UiEvent.NavigateWithData<*> -> {
                    if (event.route == Routes.AddAlarmScreen){
                        navigateToAddAlarm(event.data as Stop)
                    }
                }
                else -> Unit
            }
        }
    }

    StopPickerScreen(

        state = state.value, onAction = { action ->
            viewModel.onAction(action = action)
        }, contentPadding = contentPadding
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
        modifier = Modifier.fillMaxSize().background(AppColors.cardBackground)
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
            modifier = Modifier.fillMaxSize().padding(top=topPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = AppColors.cardBackground,
                    ),
                    modifier = Modifier.size(48.dp),
                    onClick = { onAction(StopPickerAction.NavigateBack) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = AppColors.iconTint
                    )
                }
                LocationSearchBar(query = state.query, onQueryChanged = { query ->
                    onAction(StopPickerAction.QueryChanged(query))
                }, suggestions = state.suggestions, onSuggestionClicked = { suggestion ->
                    onAction(StopPickerAction.SuggestionSelected(suggestion))
                    keyboardController?.hide()
                })
            }

            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    // Haritayı mevcut konuma odaklayın
                    onAction(StopPickerAction.CenterMapOnCurrentLocation)
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
                    .background(AppColors.cardBackground , shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = UiText.StringResourceId(Res.string.current_location).asString(),
                    tint = AppColors.iconTint
                )
            }
            AnimatedVisibility(
                visible = state.showBottom, // Her zaman görünür, ama animasyonlu gelir
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight }, // Alttan gir
                    animationSpec = tween(durationMillis = 1000)
                ) + fadeIn(animationSpec = tween(1000))
            ) {
                AdaptiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    content = CardContent.CombinedContent(
                        orientation = LayoutOrientation.VERTICAL,
                        spacing = 0.dp,
                        items = listOf(
                            // 1. Satır: EditableText + Transport Selector (HORIZONTAL layout)
                            CardContent.CombinedContent(
                                orientation = LayoutOrientation.HORIZONTAL,
                                spacing = 8.dp,
                                items = listOf(
                                    CardContent.EditableTextContent(
                                        modifier = Modifier.weight(1f),
                                        value = state.stop.name,
                                        onValueChange = { onAction(StopPickerAction.TitleChanged(it)) },
                                        placeholder = UiText.StringResourceId(Res.string.stop_title).asString(),
                                        textStyle = AppTypography.bodyRegular
                                    ), CardContent.CustomComposable {
                                        TransportTypeDropdownSelector(
                                            modifier = Modifier.size(64.dp).padding(start = 8.dp),
                                            selected = state.selectedTransportType,
                                            onSelected = { type ->
                                                onAction(StopPickerAction.SelectTransportType(type))
                                            })
                                    })),
                            // 2. Satır: Buton
                            CardContent.CustomComposable {
                                PrimaryButton(
                                    modifier = Modifier.fillMaxWidth().padding(bottomPadding),
                                    text = UiText.StringResourceId(Res.string.add_stop).asString()
                                    ,
                                    onClick = {
                                        onAction(StopPickerAction.AddStop(state.stop))
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = AppColors.buttonText,
                                        disabledContainerColor = AppColors.buttonBackground.copy(
                                            alpha = 0.5f
                                        ),
                                        disabledContentColor = AppColors.buttonText.copy(alpha = 0.5f)
                                    )
                                )
                            })),
                )

            }


        }

    }


}
