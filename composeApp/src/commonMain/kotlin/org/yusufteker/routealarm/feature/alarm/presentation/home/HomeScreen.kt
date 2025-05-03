package org.yusufteker.routealarm.feature.alarm.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onAddAlarm: () -> Unit, onAlarmClick: (String) -> Unit, onSettingsClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
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
        }
    }
}
