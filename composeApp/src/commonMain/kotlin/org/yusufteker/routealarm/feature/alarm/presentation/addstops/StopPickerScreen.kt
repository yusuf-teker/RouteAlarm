package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.button.PrimaryButton
import org.yusufteker.routealarm.core.presentation.card.AdaptiveCard
import org.yusufteker.routealarm.core.presentation.card.CardContent
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.presentation.SharedAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addstops.components.TransportTypeDropdownSelector


@Composable
fun StopPickerScreenRoot(
    viewModel: StopPickerViewModel = koinViewModel(),
    onAddStopClick: (Stop) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.value.canAddAndNavigate){ // todo kotu yontem degis
        if (state.value.canAddAndNavigate){
            onAddStopClick(state.value.stop)
        }
    }
    StopPickerScreen(

        state = state.value, onAction = { action ->
            viewModel.onAction(action = action)
            when (action) {
                //Diğer ekranlarla ilgili olanlar bu kısımda diğerleri viewmodelde
                is StopPickerAction.AddStop -> {
                    /*
                     if (state.value.canAddAndNavigate){
                                onAddStopClick(state.value.stop) // izin sonrasına eklendi
                            }
                    * */
                }
                else -> Unit
            }
        }, contentPadding = contentPadding
    )
}

@Composable
fun StopPickerScreen(
    state: StopPickerState,
    onAction: (StopPickerAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {

    /*PlatformLocationPermissionHandler{ isGranted ->
        onAction(StopPickerAction.LocationPermissionResult(isGranted))
    }*/


    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.background).padding(contentPadding)
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier, verticalAlignment = Alignment.CenterVertically
        ) {
            AdaptiveCard(
                modifier = Modifier.weight(1f), content = CardContent.EditableTextContent(
                    value = state.stop.name,
                    onValueChange = { onAction(StopPickerAction.TitleChanged(it)) },
                    placeholder = "Durak Başlığı"
                )
            )
            Spacer(modifier = Modifier.width(16.dp))

            TransportTypeDropdownSelector(
                modifier = Modifier.size(72.dp),
                selected = state.selectedTransportType,
                onSelected = { type ->
                    onAction(StopPickerAction.SelectTransportType(type))
                })
        }


        Spacer(Modifier.weight(1f))
        PrimaryButton(
            modifier = Modifier.fillMaxWidth(), text = "Durak Ekle", onClick = {
                onAction(StopPickerAction.AddStop(state.stop)) // AddAlarmScreen’e geri dön
            })
    }
}
