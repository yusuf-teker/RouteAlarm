package org.yusufteker.routealarm.feature.alarm.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.cancel
import routealarm.composeapp.generated.resources.confirm
import routealarm.composeapp.generated.resources.delete_alarm
import routealarm.composeapp.generated.resources.delete_alarm_confirm
import routealarm.composeapp.generated.resources.stop_count

@Composable
fun AlarmList(
    alarms: List<Alarm>,
    onCheckedChange: (Alarm, Boolean) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onAlarmClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(alarms, key = { it.id }) { alarm ->
            AlarmCard(
                alarm = alarm,
                onCheckedChange = { isChecked -> onCheckedChange(alarm, isChecked) },
                onClick = { onAlarmClicked(alarm.id) },
                onDeleteClick = { alarmToDelete = alarm }
            )
        }
    }

    alarmToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { alarmToDelete = null },
            containerColor = AppColors.cardBackground,
            titleContentColor = AppColors.textPrimary,
            textContentColor = AppColors.textSecondary,
            title = { Text(stringResource(Res.string.delete_alarm)) },
            text = { Text(stringResource(Res.string.delete_alarm_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAlarm(target)
                        alarmToDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.confirm),
                        color = AppColors.danger,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { alarmToDelete = null }) {
                    Text(
                        text = stringResource(Res.string.cancel),
                        color = AppColors.textSecondary
                    )
                }
            }
        )
    }
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val borderColor by animateColorAsState(
        targetValue = if (alarm.isActive) AppColors.accent.copy(alpha = 0.7f) else AppColors.border,
        label = "alarmCardBorder"
    )
    val sortedStops = remember(alarm.stops) { alarm.stops.sortedBy { it.orderIndex } }
    val firstTransport = sortedStops.firstOrNull()?.transportType
    val routePreview = remember(sortedStops) {
        sortedStops.joinToString(" → ") { it.name }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.cardBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = if (alarm.isActive) {
                            listOf(
                                AppColors.cardBackground,
                                AppColors.accent.copy(alpha = 0.08f)
                            )
                        } else {
                            listOf(AppColors.cardBackground, AppColors.cardBackground)
                        }
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (alarm.isActive) AppColors.accent.copy(alpha = 0.16f)
                            else AppColors.cardBackgroundSecondary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (firstTransport != null) {
                        Image(
                            painter = painterResource(firstTransport.iconRes),
                            contentDescription = firstTransport.name,
                            colorFilter = ColorFilter.tint(
                                if (alarm.isActive) AppColors.accent else AppColors.textPrimary
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = alarm.title,
                        style = AppTypography.titleMedium.copy(fontSize = 18.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (routePreview.isNotBlank()) {
                        Text(
                            text = routePreview,
                            style = AppTypography.labelSmall.copy(
                                color = AppColors.textSecondary,
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.stop_count, sortedStops.size),
                            style = AppTypography.labelSmall.copy(
                                color = if (alarm.isActive) AppColors.accentSoft else AppColors.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        if (alarm.isVibration) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = AppColors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.delete_alarm),
                        tint = AppColors.textSecondary
                    )
                }

                Switch(
                    checked = alarm.isActive,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.onAccent,
                        checkedTrackColor = AppColors.accent,
                        uncheckedThumbColor = AppColors.textSecondary,
                        uncheckedTrackColor = AppColors.cardBackgroundSecondary,
                        uncheckedBorderColor = AppColors.border
                    )
                )
            }
        }
    }
}
