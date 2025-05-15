package org.yusufteker.routealarm.feature.alarm.presentation.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.yusufteker.routealarm.core.data.dummy.startingStop
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.map_pin

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.getValue

@Composable
fun ActiveAlarm(alarm: Alarm){
    Column(
        modifier = Modifier
            .fillMaxWidth().background(AppColors.activeAlarmBarBackground),
    ) {

        val stops = listOf(startingStop) + alarm.stops
        val passedIndex = stops.count { it.isPassed } - 1

        val dotSize = 20.dp
            val dotRadius = dotSize / 2
            val segmentLength = 100.dp
        val scrollState = rememberScrollState()
            val scrollModifier = if (stops.size > 2) {
                Modifier.horizontalScroll(scrollState)
            } else Modifier

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Stop Names
                Row(
                    modifier = scrollModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    stops.forEachIndexed { index, stop ->
                        Box(
                            modifier = Modifier.width(dotSize*2 + segmentLength),
                            contentAlignment = Alignment.BottomStart
                        ) {

                            Text(
                                text = stop.name,
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,

                                )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dots and Segments
                Row(
                    modifier = scrollModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    stops.forEachIndexed { index, _ ->


                        if (index == 0){
                            Box(
                                modifier = Modifier
                                    .size(dotSize)
                                    .clip(CircleShape)
                                    .background(
                                        Color.Green
                                    )
                            )
                        }else{
                            Image(
                                painter = painterResource(Res.drawable.map_pin),
                                contentDescription = "Map Pin",
                                modifier = Modifier.size(dotSize),
                                colorFilter = ColorFilter.tint(when {
                                    index < passedIndex -> Color.Green
                                    index == passedIndex -> Color.Green
                                    else -> Color.Red
                                })
                            )
                        }

                        // Segment (if not last)
                        if (index < stops.lastIndex) {
                            val progress = when {
                                index < passedIndex -> 1f
                                index == passedIndex -> {
                                    val infiniteTransition = rememberInfiniteTransition()
                                    infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 0.8f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(durationMillis = 1500, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        )
                                    ).value
                                }
                                else -> 0f
                            }

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = dotRadius)
                                    .width(segmentLength)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.Gray.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .background(Color.Green)
                                )
                            }
                        }
                    }
                }
            }
        }

}


@Composable
fun ActiveAlarmMax2Stop(
    alarm: Alarm,
    modifier: Modifier = Modifier
) {
    val stops = listOf(startingStop) + alarm.stops

    val passedStops = stops.count { it.isPassed } - 1
    val segmentCount = stops.size

    Column(
        modifier = modifier.fillMaxWidth().background(
            AppColors.activeAlarmBarBackground
        ).padding(16.dp),
    ) {


        Text(
            text = alarm.title,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stops.forEachIndexed { index, stop ->
                Box(
                    modifier = Modifier.weight(1f), contentAlignment = when (index) {
                        0 -> Alignment.CenterStart // İlk eleman sola
                        stops.lastIndex -> Alignment.CenterEnd // Son eleman sağa
                        else -> Alignment.Center // Diğerleri ortada
                    }
                ) {
                    Text(
                        text = stop.name,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }


            }


        }
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in 0 until segmentCount) {
                // Nokta (başlangıç noktası daima yeşil)
                val isStopReached = i <= passedStops
                if (i == 0) {
                    Box(
                        modifier = Modifier.size(24.dp).clip(CircleShape).background(
                            if (isStopReached) Color.Green else Color.Gray
                        )
                    )
                } else {

                    Image(
                        painter = painterResource(Res.drawable.map_pin),
                        contentDescription = "Map Pin",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(if (isStopReached) Color.Green else Color.Red)
                    )
                }


                // Noktalar arası Progress çubukları
                if (i < segmentCount - 1) {
                    val segmentProgress = when {
                        passedStops > i -> 1f     // tamamen dolu
                        passedStops == i -> 0.5f // yarısı dolu
                        else -> 0f                  // boş
                    }
                    Box(
                        modifier = Modifier.weight(1f).height(4.dp).padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.Gray.copy(alpha = 0.3f))
                    ) {
                        if (passedStops != i) {

                            Box(
                                modifier = Modifier.fillMaxHeight()
                                    .fillMaxWidth(fraction = segmentProgress)
                                    .background(Color.Green)
                            )

                        } else {
                            val infiniteTransition = rememberInfiniteTransition()
                            val animatedProgress by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 0.8f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        durationMillis = 3000, easing = FastOutSlowInEasing
                                    ), repeatMode = RepeatMode.Restart
                                )
                            )

                            Box(
                                modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedProgress)
                                    .background(Color.Green)
                            )
                        }
                    }

                }
            }
        }
    }

}