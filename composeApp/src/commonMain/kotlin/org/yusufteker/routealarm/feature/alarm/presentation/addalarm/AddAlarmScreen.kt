package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.core.presentation.button.PrimaryButton
import org.yusufteker.routealarm.core.presentation.card.AdaptiveCard
import org.yusufteker.routealarm.core.presentation.card.CardContent
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.presentation.SharedAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.AddStopCard
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.StopCard


@Composable
fun AddAlarmScreenRoot(
    viewModel: AddAlarmViewModel = koinViewModel(),
    navigateToStopPicker: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    navigateToHome: () -> Unit
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event){
                is UiEvent.NavigateTo -> {
                    if (event.route == Routes.StopPickerScreen) {
                        navigateToStopPicker()
                    }else if (event.route == Routes.HomeScreen){
                        navigateToHome()
                    }
                }
                else -> Unit
            }
        }
    }

    AddAlarmScreen(
        state = state.value, onAction = { action ->
            viewModel.onAction(action = action)
        }, contentPadding = contentPadding
    )
}

@Composable
fun AddAlarmScreen(
    state: AddAlarmState,
    onAction: (AddAlarmAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {

    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.background).padding(contentPadding)
            .padding(16.dp)
    ) {
        AdaptiveCard(
            content = CardContent.EditableTextContent(
                value = state.title,
                onValueChange = { onAction(AddAlarmAction.TitleChanged(it)) },
                placeholder = "Alarm Başlığı"
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        StopList(
            stops = state.stops,
            onRemove = { stop -> onAction(AddAlarmAction.RemoveStop(stop)) },
            onAddClick = {
                onAction(AddAlarmAction.CheckLocationPermission)
            })

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Kaydet",
            onClick = {
                onAction(AddAlarmAction.SaveAlarm)
            }

        )
    }
}


@Composable
fun StopList(
    stops: List<Stop>, onRemove: (Stop) -> Unit, onAddClick: () -> Unit
) {
    Column {
        stops.forEachIndexed { index, stop ->
            StopItem(
                stop = stop, index = index + 1, onRemove = { onRemove(stop) })
        }

        AddStopCard(onClick = onAddClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopItem(
    stop: Stop, index: Int, onRemove: () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else {
                false
            }
        })

    SwipeToDismissBox(state = state, enableDismissFromStartToEnd = false, backgroundContent = {

        Box(Modifier.fillMaxSize().background(Color.Transparent)) {
            Card(
                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Durağı Sil",
                        tint = Color.White
                    )
                }


            }
        }


    }, content = {
        StopCard(
            stop = stop, index = index, modifier = Modifier.padding(vertical = 4.dp)
        )
    })
}

