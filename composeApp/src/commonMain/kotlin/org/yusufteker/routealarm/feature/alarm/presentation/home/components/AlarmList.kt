package org.yusufteker.routealarm.feature.alarm.presentation.home.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteAlarmItem(
    alarm: Alarm, onCheckedChange: (Boolean) -> Unit, onDelete: () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        })

    SwipeToDismissBox(state = state, enableDismissFromStartToEnd = false, backgroundContent = {
        // Silme arka planı
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Red).padding(end = 24.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Alarm",
                tint = Color.White
            )
        }
    }, content = {
        AlarmItemContent(
            alarm = alarm,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.background(AppColors.cardBackground)
        )
    })
}

@Composable
private fun AlarmItemContent(
    alarm: Alarm, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = alarm.title, style = AppTypography.titleMedium
                )
                Text(
                    text = "Durak sayısı: ${alarm.stops.size}", style = AppTypography.bodyRegular
                )
            }
            Switch(
                checked = alarm.isActive,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.green,
                    checkedTrackColor = AppColors.background,
                )
            )
        }
    }
}

@Composable
fun AlarmList(
    modifier: Modifier = Modifier,
    alarms: List<Alarm>,
    onAlarmCheckedChange: (Alarm, Boolean) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = "Alarmlar",
            style = AppTypography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(alarms, key = { it.id }) { alarm ->
                SwipeToDeleteAlarmItem(
                    alarm = alarm,
                    onCheckedChange = { onAlarmCheckedChange(alarm, it) },
                    onDelete = { onDeleteAlarm(alarm) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}