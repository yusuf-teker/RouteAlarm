package org.yusufteker.routealarm.feature.alarm.presentation.home

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.core.data.dummy.fakeAlarms
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.ActiveAlarm

@Composable
fun HomeScreen(
    onAddAlarm: () -> Unit, onAlarmClick: (String) -> Unit, onSettingsClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {

    Column(
        modifier = Modifier.fillMaxSize().background(
            AppColors.background
        ).padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        Text("Home Screen")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddAlarm) {
            Text("Add Alarm")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onAlarmClick("sample-id") }) {
            Text("Alarm Detail")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSettingsClick) {
            Text("Settings")
        }
        Spacer(Modifier.weight(1f))
        ActiveAlarm(fakeAlarms.first())
    }

}


