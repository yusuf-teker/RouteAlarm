package org.yusufteker.routealarm.feature.alarm.presentation.home

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.StopProgressBar

@Composable
fun HomeScreen(
    onAddAlarm: () -> Unit, onAlarmClick: (String) -> Unit, onSettingsClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
            ActiveAlarm()
        }
    }
}


@Composable
fun ActiveAlarm(){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212)) // Koyu arka plan
            .padding(vertical = 16.dp)
    ) {

        StopProgressBar(
            title = "Başlık",
            passedStops = 1,
            stopNames = listOf("Start", "Destination1", "Destination2","Destination3"),
            )

    }
}