package org.yusufteker.routealarm.feature.alarm.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.core.presentation.AppTypography

@Composable
fun EmptyHomeScreen(
    onAddAlarmClick: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Henüz bir alarm eklemediniz", style = AppTypography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onAddAlarmClick() }) {
            Text("Alarm Ekle")
        }
    }
}