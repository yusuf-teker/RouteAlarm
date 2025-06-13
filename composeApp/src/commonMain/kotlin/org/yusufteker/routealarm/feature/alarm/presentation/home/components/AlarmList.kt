package org.yusufteker.routealarm.feature.alarm.presentation.home.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.core.presentation.UiText
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.alarms
import routealarm.composeapp.generated.resources.stop_count


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteAlarmItem(
    alarm: Alarm, onCheckedChange: (Boolean) -> Unit, onDelete: () -> Unit,
    modifier: Modifier
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
            modifier = Modifier.fillMaxSize().background(Color.Red, shape = MaterialTheme.shapes.medium).padding(end = 24.dp),
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
            modifier = modifier.background(AppColors.cardBackground, shape = MaterialTheme.shapes.medium)
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
                    text = stringResource(Res.string.stop_count,alarm.stops.size ), style = AppTypography.bodyRegular.copy(
                        color = AppColors.textPrimary.copy(alpha = 0.5f)
                    )
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
    onDeleteAlarm: (Alarm) -> Unit,
    onAlarmClick: (Int) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = UiText.StringResourceId(Res.string.alarms).asString(),
            style = AppTypography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(alarms, key = { it.id }) { alarm ->
                SwipeToDeleteAlarmItem(
                    alarm = alarm,
                    onCheckedChange = { onAlarmCheckedChange(alarm, it) },
                    onDelete = { onDeleteAlarm(alarm) },
                    modifier = Modifier.clickable{ onAlarmClick(alarm.id)}
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}