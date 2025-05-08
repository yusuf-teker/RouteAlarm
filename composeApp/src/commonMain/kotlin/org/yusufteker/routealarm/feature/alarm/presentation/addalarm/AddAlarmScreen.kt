package org.yusufteker.routealarm.feature.alarm.presentation.addalarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.card.AdaptiveCard
import org.yusufteker.routealarm.core.presentation.card.CardContent
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.AddStopCard
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.StopCard

@Composable
fun AddAlarmScreenRoot(
    viewModel: AddAlarmViewModel = koinViewModel(),
    onAddStopClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    AddAlarmScreen(
        state = state.value,
        onAction = { action ->
            when (action) {
                //Diğer ekranlarla ilgili olanlar bu kısımda diğerleri viewmodelde
                is AddAlarmAction.AddStop -> onAddStopClick()
                else -> Unit
            }
            viewModel.onAction(action = action)
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
            .padding(contentPadding)
            .padding(16.dp)
    ) {
        AdaptiveCard(
            content = CardContent.EditableTextContent(
                value = state.title,
                onValueChange = {onAction(AddAlarmAction.TitleChanged(it)) },
                placeholder = "Alarm Başlığı"
            )
        )


        Spacer(modifier = Modifier.height(16.dp))

        Column {
            state.stops.forEachIndexed { index, stop ->
                StopCard(
                    stop = stop,
                    index = index + 1,
                    onRemove = {
                        onAction(AddAlarmAction.RemoveStop(stop))
                    }
                )
            }

            AddStopCard(onClick ={  onAction(AddAlarmAction.AddStop)} )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onAction(AddAlarmAction.SaveAlarm) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kaydet")
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.errorMessage, color = Color.Red)
        }
    }
}
