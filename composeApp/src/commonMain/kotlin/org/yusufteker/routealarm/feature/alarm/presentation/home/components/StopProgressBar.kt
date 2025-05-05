package org.yusufteker.routealarm.feature.alarm.presentation.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.map_pin

@Composable
fun StopProgressBar(
    alarm: Alarm,
    modifier: Modifier = Modifier
) {
    val stops = alarm.stops
    val passedStops = stops.count { it.isPassed }
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