package org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.UiText
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.StopCard
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.StopStatus
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components.rememberShimmerBrush
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.SwipeToDeleteAlarmItem
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.alarm_detail
import routealarm.composeapp.generated.resources.remaining_distance
import kotlin.math.ceil
import kotlin.math.min


@Composable
fun AlarmDetailScreenRoot(
    viewModel: AlarmDetailViewModel = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
    alarmId: Int
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(AlarmDetailAction.LoadData(alarmId))

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .padding(contentPadding)
            .padding(16.dp)
    ) {
        Text(
            text = state.alarm?.title ?: UiText.StringResourceId(Res.string.alarm_detail).asString()
            ,
            style = MaterialTheme.typography.titleLarge
        )

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


        Spacer(modifier = Modifier.height(16.dp))

        if (state.isAlarmActive) {

            state.previousStops.forEach {
                StopCard(
                    stop = it,
                    index = it.orderIndex,
                    modifier = Modifier.padding(vertical = 4.dp),
                    status = StopStatus.Passed
                )
            }
            Spacer(Modifier.height(16.dp))

            if (state.isLoading){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }else{
                RouteProgressBar(
                    state.progress,
                    state.remainingDistance,
                    state.activeStop,
                )

                Spacer(Modifier.height(16.dp))
            }


            state.nextStops.forEach {
                StopCard(
                    stop = it,
                    index = it.orderIndex,
                    modifier = Modifier.padding(vertical = 4.dp),
                    status = StopStatus.Upcoming
                )
            }
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



