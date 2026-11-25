package org.yusufteker.routealarm.feature.alarm.presentation.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.modifier.rgbBorder
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.alarm_active_banner
import routealarm.composeapp.generated.resources.map_pin
import routealarm.composeapp.generated.resources.start_point
import routealarm.composeapp.generated.resources.tap_for_details
import kotlin.math.roundToInt

@Composable
fun ActiveAlarm(
    alarm: Alarm,
    modifier: Modifier = Modifier,
    activeStopIndex: Int = -1,
    activeStopProgress: Float = 0f,
    remainingDistanceMeters: Double? = null,
    onClick: () -> Unit = {}
) {
    val stops = remember(alarm.stops) { alarm.stops.sortedBy { it.orderIndex } }
    val displayStops = remember(stops) { stops.take(4) }
    val resolvedActiveIndex = remember(displayStops, activeStopIndex) {
        if (activeStopIndex >= 0) activeStopIndex
        else displayStops.indexOfFirst { !it.isPassed }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.activeAlarmBarBackground,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .rgbBorder(strokeWidth = 1.5.dp, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.cardBackgroundSecondary.copy(alpha = 0.7f),
                            AppColors.activeAlarmBarBackground
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AppColors.accent)
                    )
                    Text(
                        text = alarm.title,
                        style = AppTypography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (remainingDistanceMeters != null) {
                        val percent = (activeStopProgress.coerceIn(0f, 1f) * 100f).roundToInt()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.accent.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "${formatDistance(remainingDistanceMeters)} · %$percent",
                                style = AppTypography.labelSmall.copy(
                                    color = AppColors.accentSoft,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(Res.string.alarm_active_banner),
                            style = AppTypography.labelSmall.copy(
                                color = AppColors.accentSoft,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(Res.string.tap_for_details),
                        tint = AppColors.accentSoft,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Origin point
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AppColors.accent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.map_pin),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(AppColors.accent),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(Res.string.start_point),
                        style = AppTypography.labelSmall.copy(color = AppColors.accentSoft),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                displayStops.forEachIndexed { index, stop ->
                    val targetFraction = when {
                        stop.isPassed -> 1f
                        index == resolvedActiveIndex -> activeStopProgress.coerceIn(0f, 1f)
                        else -> 0f
                    }
                    val animatedFraction by animateFloatAsState(
                        targetValue = targetFraction,
                        animationSpec = tween(durationMillis = 500)
                    )
                    val isCurrentTarget = !stop.isPassed && index == resolvedActiveIndex

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 15.dp, start = 4.dp, end = 4.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(AppColors.border),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (animatedFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedFraction.coerceAtLeast(0.04f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(AppColors.accent, AppColors.accentSoft)
                                        )
                                    )
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(64.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        stop.isPassed -> AppColors.accent
                                        isCurrentTarget -> AppColors.accent.copy(alpha = 0.14f)
                                        else -> AppColors.cardBackgroundSecondary
                                    }
                                )
                                .border(
                                    width = if (isCurrentTarget) 2.dp else 1.5.dp,
                                    color = when {
                                        stop.isPassed -> AppColors.accent
                                        isCurrentTarget -> AppColors.accentSoft
                                        else -> AppColors.border
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(stop.transportType.iconRes),
                                contentDescription = stop.name,
                                colorFilter = ColorFilter.tint(
                                    when {
                                        stop.isPassed -> AppColors.onAccent
                                        isCurrentTarget -> AppColors.accentSoft
                                        else -> AppColors.textPrimary
                                    }
                                ),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stop.name,
                            style = AppTypography.labelSmall.copy(
                                color = when {
                                    stop.isPassed || isCurrentTarget -> AppColors.accentSoft
                                    else -> AppColors.textPrimary
                                },
                                fontWeight = if (isCurrentTarget) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
