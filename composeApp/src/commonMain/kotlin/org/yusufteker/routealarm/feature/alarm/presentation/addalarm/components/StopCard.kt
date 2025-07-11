package org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.feature.alarm.domain.Stop

import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import org.yusufteker.routealarm.core.presentation.UiText
import org.yusufteker.routealarm.core.presentation.brush.rememberShimmerBrush
import org.yusufteker.routealarm.core.presentation.modifier.activeAlarmBorderColors
import org.yusufteker.routealarm.core.presentation.modifier.rainbowColors
import org.yusufteker.routealarm.core.presentation.modifier.rgbBorder
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.stop_with_index

@Composable
fun StopCard(
    stop: Stop,
    index: Int,
    textColor: Color = AppColors.textPrimary,
    modifier: Modifier = Modifier,
    status: StopStatus = StopStatus.Upcoming
) {
    val shimmerBrush = rememberShimmerBrush()

    val backgroundModifier = when (status) {
        StopStatus.InProgress -> modifier

            .background(shimmerBrush, shape = RoundedCornerShape(16.dp))
            .rgbBorder(
                strokeWidth = 4.dp,
                shape = RoundedCornerShape(16.dp),
                duration = 5000,
                colorList = activeAlarmBorderColors
            )
        else -> modifier
            .background(
                color = when (status) {
                    StopStatus.Passed -> AppColors.darkGreen.copy(alpha = 0.5f)
                    StopStatus.Upcoming -> AppColors.cardBackground
                    else -> AppColors.cardBackground
                },
                shape = RoundedCornerShape(16.dp)
            )
    }

    val contentAlpha = when (status) {
        StopStatus.Passed -> 0.7f
        else -> 1f
    }

    val iconTint = when (status) {
        StopStatus.Passed -> Color(0xFFE8F5E9)
        else -> Color.White
    }

    Card(
        modifier = backgroundModifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Arka planı dışarıdan alıyoruz
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = UiText.StringResourceId(Res.string.stop_with_index, arrayOf(index)).asString(),
                    style = AppTypography.titleMedium.copy(color = textColor.copy(alpha = contentAlpha))
                )
                Text(
                    text = stop.name,
                    style = AppTypography.bodyRegular.copy(
                        color = textColor.copy(alpha = contentAlpha * 0.6f)
                    )
                )
            }
            Image(
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp),
                painter = painterResource(stop.transportType.iconRes),
                contentDescription = stop.name,
                colorFilter = ColorFilter.tint(iconTint)
            )
        }
    }
}



enum class StopStatus {
    Passed,
    InProgress,
    Upcoming
}
