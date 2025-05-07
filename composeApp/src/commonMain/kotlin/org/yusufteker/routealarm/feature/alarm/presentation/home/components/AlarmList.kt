package org.yusufteker.routealarm.feature.alarm.presentation.home.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.feature.alarm.domain.Alarm

@Composable
fun AlarmList(
    modifier: Modifier = Modifier,
    list: List<Alarm>,
    onAlarmCheckedChange: (Alarm, Boolean) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = "Alarmlar",
            style = AppTypography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp).align(
                Alignment.CenterHorizontally
            )
        )


            LazyColumn {
                items(list) { alarm ->
                    AlarmItem(alarm = alarm, onCheckedChange = {
                        onAlarmCheckedChange(alarm, it)
                    })
                }
            }

    }
}

@Composable
fun AlarmItem(
    alarm: Alarm,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = alarm.title,
                style = AppTypography.titleMedium
            )
            Text(
                text = "Durak sayısı: ${alarm.stops.size}",
                style = AppTypography.bodyRegular
            )
        }
        Switch(
            checked = alarm.isActive,
            onCheckedChange = onCheckedChange
        )
    }
}
