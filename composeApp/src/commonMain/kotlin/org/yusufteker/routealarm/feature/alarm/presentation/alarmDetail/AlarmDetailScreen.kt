package org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.UiEvent
import org.yusufteker.routealarm.core.presentation.UiText
import org.yusufteker.routealarm.core.presentation.brush.rememberShimmerBrush
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.StopCard
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.StopStatus
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.SwipeToDeleteAlarmItem
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.alarm_detail
import routealarm.composeapp.generated.resources.current_stop_title
import routealarm.composeapp.generated.resources.passed_stops_title
import routealarm.composeapp.generated.resources.remaining_distance
import routealarm.composeapp.generated.resources.upcoming_stops_title


@Composable
fun AlarmDetailScreenRoot(
    viewModel: AlarmDetailViewModel = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    alarmId: Int,
    onBackClick: () -> Unit,

    ) {
    val state by viewModel.state.collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {
        viewModel.onAction(AlarmDetailAction.LoadData(alarmId))
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> {
                    onBackClick()
                }

                else -> Unit
            }
        }

    }


    AlarmDetailScreen(
        state = state,
        onAction = { viewModel.onAction(it) },
        contentPadding = contentPadding
    )
}


@Composable
fun AlarmDetailScreen(
    state: AlarmDetailState,
    onAction: (AlarmDetailAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {

    var showPreviousStops by remember { mutableStateOf(false) }
    var showNextStops by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = AppColors.cardBackground,
                ),
                modifier = Modifier.size(48.dp),
                onClick = { onAction(AlarmDetailAction.NavigateBack) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = AppColors.iconTint
                )
            }
            Text(
                text = state.alarm?.title ?: UiText.StringResourceId(Res.string.alarm_detail)
                    .asString(),
                modifier = Modifier.weight(1f).padding(start = 16.dp, end = 8.dp),
                style = AppTypography.titleLarge,
                color = AppColors.textPrimary
            )
        }
        Column(modifier = Modifier.weight(1f)) {

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {

                if (state.isAlarmActive) {


                    // Current Stop
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = UiText.StringResourceId(Res.string.current_stop_title).asString(),
                        style = AppTypography.titleMedium,
                        color = AppColors.textPrimary,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    RouteProgressBar(
                        state.progress,
                        state.remainingDistance,
                        state.activeStop,
                    )

                    // Previous Stops
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.cardBackground)
                            .clickable { showPreviousStops = !showPreviousStops }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = UiText.StringResourceId(Res.string.passed_stops_title)
                                .asString(),
                            style = AppTypography.titleMedium,
                            color = AppColors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppColors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (showPreviousStops) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.textPrimary
                            )
                        }
                    }
                    AnimatedVisibility(visible = showPreviousStops) {
                        Column {
                            state.previousStops.forEach {
                                StopCard(
                                    stop = it,
                                    index = it.orderIndex,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    status = StopStatus.Passed
                                )
                            }
                        }
                    }

                    // Next Stops
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.cardBackground)
                            .clickable { showNextStops = !showNextStops }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = UiText.StringResourceId(Res.string.upcoming_stops_title)
                                .asString(),
                            style = AppTypography.titleMedium,
                            color = AppColors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppColors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (showNextStops) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.textPrimary
                            )
                        }
                    }
                    AnimatedVisibility(visible = showNextStops) {
                        Spacer(Modifier.height(16.dp))
                        Column {
                            state.nextStops.forEach {
                                StopCard(
                                    stop = it,
                                    index = it.orderIndex,
                                    textColor = AppColors.textSecondary,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    status = StopStatus.Upcoming
                                )
                            }
                        }
                    }
                }


            }


        }


        state.alarm?.let {
            SwipeToDeleteAlarmItem(
                alarm = state.alarm,
                onCheckedChange = { isChecked ->
                    onAction(AlarmDetailAction.OnAlarmCheckedChange(state.alarm, isChecked))
                },
                onDelete = {
                    onAction(AlarmDetailAction.OnDeleteAlarm(state.alarm.id))
                },
                modifier = Modifier
            )
        }


    }
}

@Composable
fun RouteProgressBar(
    progress: Float,
    remainingDistance: Float?,
    activeStop: Stop?,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val shimmerBrush = rememberShimmerBrush()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üstte: Durak adı ve kalan mesafe
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${activeStop?.name}",
                style = AppTypography.bodyRegular,
                color = AppColors.textPrimary,
            )
            Spacer(Modifier.width(16.dp))
            remainingDistance?.let {
                Text(
                    text = UiText.StringResourceId(
                        Res.string.remaining_distance,
                        arrayOf(formatDistance(it.toDouble()))
                    ).asString(),
                    style = AppTypography.bodyRegular,
                    color = AppColors.textPrimary,
                )
            }
        }

        // Progress Bar (shimmerli tek parça bar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Gray.copy(alpha = 0.2f)) // Kalan kısım
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(shimmerBrush)
            )
        }

        Spacer(Modifier.height(8.dp))

        // StopCard
        activeStop?.let {
            StopCard(
                stop = it,
                index = it.orderIndex,
                modifier = Modifier.padding(vertical = 4.dp),
                status = StopStatus.InProgress
            )
        }
    }
}



