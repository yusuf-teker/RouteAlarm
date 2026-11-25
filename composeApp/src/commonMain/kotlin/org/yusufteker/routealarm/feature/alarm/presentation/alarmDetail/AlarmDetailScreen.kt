package org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.core.presentation.button.PrimaryButton
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.active
import routealarm.composeapp.generated.resources.alarm_detail
import routealarm.composeapp.generated.resources.cancel
import routealarm.composeapp.generated.resources.confirm
import routealarm.composeapp.generated.resources.current_stop_title
import routealarm.composeapp.generated.resources.delete_alarm
import routealarm.composeapp.generated.resources.delete_alarm_confirm
import routealarm.composeapp.generated.resources.go_back
import routealarm.composeapp.generated.resources.inactive
import routealarm.composeapp.generated.resources.passed_stops_title
import routealarm.composeapp.generated.resources.remaining_distance
import routealarm.composeapp.generated.resources.route_stops
import routealarm.composeapp.generated.resources.start_alarm
import routealarm.composeapp.generated.resources.stop_alarm
import routealarm.composeapp.generated.resources.stop_count
import routealarm.composeapp.generated.resources.upcoming_stops_title
import routealarm.composeapp.generated.resources.waiting_for_gps
import kotlin.math.roundToInt

private enum class TimelineStopStatus {
    PASSED,
    ACTIVE,
    UPCOMING
}

@Composable
fun AlarmDetailScreenRoot(
    viewModel: AlarmDetailViewModel = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    alarmId: Int,
    onBackClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(alarmId) {
        viewModel.onAction(AlarmDetailAction.LoadData(alarmId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> onBackClick()
                else -> Unit
            }
        }
    }

    AlarmDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        contentPadding = contentPadding
    )
}

@Composable
fun AlarmDetailScreen(
    state: AlarmDetailState,
    onAction: (AlarmDetailAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val alarm = state.alarm
    val sortedStops = remember(alarm?.stops) {
        alarm?.stops?.sortedBy { it.orderIndex }.orEmpty()
    }
    val nextStopId = state.activeStop?.id ?: sortedStops.firstOrNull { !it.isPassed }?.id

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onAction(AlarmDetailAction.NavigateBack) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColors.cardBackground)
                    .border(1.dp, AppColors.border, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.go_back),
                    tint = AppColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm?.title ?: stringResource(Res.string.alarm_detail),
                    style = AppTypography.titleLarge.copy(fontSize = 22.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        if (state.isAlarmActive) Res.string.active else Res.string.inactive
                    ),
                    style = AppTypography.labelSmall.copy(
                        color = if (state.isAlarmActive) AppColors.accentSoft else AppColors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            if (alarm != null) {
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppColors.cardBackground)
                        .border(1.dp, AppColors.border, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.delete_alarm),
                        tint = AppColors.danger
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (alarm == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.accent)
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Live progress card when active
            if (state.isAlarmActive) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.cardBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AppColors.accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppColors.accent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (state.isLocationNotFound || state.currentLocation == null) {
                                            Icons.Default.LocationOn
                                        } else {
                                            Icons.Default.Notifications
                                        },
                                        contentDescription = null,
                                        tint = AppColors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(Res.string.current_stop_title),
                                        style = AppTypography.labelSmall.copy(color = AppColors.accentSoft)
                                    )
                                    Text(
                                        text = state.activeStop?.name
                                            ?: sortedStops.firstOrNull { !it.isPassed }?.name
                                            ?: alarm.title,
                                        style = AppTypography.titleMedium.copy(fontSize = 17.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (state.currentLocation != null && !state.isLocationNotFound) {
                                LinearProgressIndicator(
                                    progress = { state.progress.coerceIn(0f, 1f) },
                                    color = AppColors.accent,
                                    trackColor = AppColors.cardBackgroundSecondary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(
                                            Res.string.remaining_distance,
                                            formatDistance(state.remainingDistance.toDouble())
                                        ),
                                        style = AppTypography.bodyRegular.copy(
                                            color = AppColors.textPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "${(state.progress.coerceIn(0f, 1f) * 100).roundToInt()}%",
                                        style = AppTypography.bodyRegular.copy(
                                            color = AppColors.accentSoft,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (state.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = AppColors.accent
                                        )
                                    }
                                    Text(
                                        text = stringResource(Res.string.waiting_for_gps),
                                        style = AppTypography.bodyRegular.copy(color = AppColors.textSecondary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Route stops header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.route_stops),
                        style = AppTypography.titleMedium.copy(fontSize = 17.sp)
                    )
                    Text(
                        text = stringResource(Res.string.stop_count, sortedStops.size),
                        style = AppTypography.labelSmall.copy(color = AppColors.accentSoft)
                    )
                }
            }

            itemsIndexed(
                items = sortedStops,
                key = { index, stop -> "${stop.id}_$index" }
            ) { index, stop ->
                val status = when {
                    stop.isPassed -> TimelineStopStatus.PASSED
                    stop.id == nextStopId -> TimelineStopStatus.ACTIVE
                    else -> TimelineStopStatus.UPCOMING
                }

                TimelineStopItem(
                    stop = stop,
                    index = index,
                    status = status,
                    isActiveAlarm = state.isAlarmActive
                )
            }
        }

        // Start / Stop button
        if (state.isAlarmActive) {
            Button(
                onClick = { onAction(AlarmDetailAction.OnAlarmCheckedChange(alarm, false)) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.danger.copy(alpha = 0.16f),
                    contentColor = AppColors.danger
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(1.dp, AppColors.danger.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = stringResource(Res.string.stop_alarm),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            PrimaryButton(
                text = stringResource(Res.string.start_alarm),
                onClick = { onAction(AlarmDetailAction.OnAlarmCheckedChange(alarm, true)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDeleteConfirm && alarm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = AppColors.cardBackground,
            titleContentColor = AppColors.textPrimary,
            textContentColor = AppColors.textSecondary,
            title = { Text(stringResource(Res.string.delete_alarm)) },
            text = { Text(stringResource(Res.string.delete_alarm_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onAction(AlarmDetailAction.OnDeleteAlarm(alarm.id))
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
                TextButton(onClick = { showDeleteConfirm = false }) {
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
private fun TimelineStopItem(
    stop: Stop,
    index: Int,
    status: TimelineStopStatus,
    isActiveAlarm: Boolean
) {
    val isHighlighted = isActiveAlarm && status == TimelineStopStatus.ACTIVE
    val borderColor = when {
        isHighlighted -> AppColors.accent
        status == TimelineStopStatus.PASSED -> AppColors.accent.copy(alpha = 0.35f)
        else -> AppColors.border
    }

    val statusText = when {
        status == TimelineStopStatus.PASSED -> stringResource(Res.string.passed_stops_title)
        isHighlighted -> stringResource(Res.string.current_stop_title)
        else -> stringResource(Res.string.upcoming_stops_title)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isHighlighted) AppColors.cardBackgroundSecondary else AppColors.cardBackground,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            status == TimelineStopStatus.PASSED -> AppColors.accent
                            isHighlighted -> AppColors.accent.copy(alpha = 0.2f)
                            else -> AppColors.surfaceElevated
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (status == TimelineStopStatus.PASSED) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AppColors.onAccent,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        style = AppTypography.bodyRegular.copy(
                            color = if (isHighlighted) AppColors.accentSoft else AppColors.textSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.surfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(stop.transportType.iconRes),
                    contentDescription = stop.transportType.name,
                    colorFilter = ColorFilter.tint(
                        if (isHighlighted || status == TimelineStopStatus.PASSED) AppColors.accentSoft
                        else AppColors.textPrimary
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stop.name,
                    style = AppTypography.titleMedium.copy(fontSize = 15.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActiveAlarm) {
                    Text(
                        text = statusText,
                        style = AppTypography.labelSmall.copy(
                            color = if (isHighlighted || status == TimelineStopStatus.PASSED) {
                                AppColors.accentSoft
                            } else {
                                AppColors.textSecondary
                            }
                        )
                    )
                }
            }
        }
    }
}
