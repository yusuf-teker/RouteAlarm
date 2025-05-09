package org.yusufteker.routealarm.feature.alarm.presentation.addstops

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.presentation.SharedAlarmViewModel


@Composable
fun StopPickerScreenRoot(
    viewModel: StopPickerViewModel = koinViewModel(),
    onAddStopClick: (Stop) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    StopPickerScreen(
        state = state.value,
        onAction = { action ->
            when (action) {
                //Diğer ekranlarla ilgili olanlar bu kısımda diğerleri viewmodelde
                is StopPickerAction.AddStop -> {
                    onAddStopClick(action.stop)
                }
                else -> Unit
            }
            viewModel.onAction(action = action)
        },
        contentPadding = contentPadding
    )
}
@Composable
fun StopPickerScreen(
    state: StopPickerState,
    onAction: (StopPickerAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val sharedViewModel: SharedAlarmViewModel = koinViewModel()

    Column(
        modifier = Modifier.padding(contentPadding)
    ) {
        Text("Durak Seçme Ekranı")

        Button(onClick = {
            // todo alarm eklenecek alarm id  elimizde olacak sonra
            val newStop = Stop(id = -1, name = "Otogar", latitude = 0.0, longitude = 0.0, alarmId = -1)
            sharedViewModel.addStop(newStop)

            onAction(StopPickerAction.AddStop(newStop)) // AddAlarmScreen’e geri dön
        }) {
            Text("Otogar’ı Ekle ve Geri Dön")
        }
    }
}
