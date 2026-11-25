package org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.delete_stop
import routealarm.composeapp.generated.resources.stop_with_index

@Composable
fun StopCard(
    stop: Stop,
    index: Int,
    onDeleteStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.cardBackground,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
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
                    .background(AppColors.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = AppTypography.bodyRegular.copy(
                        color = AppColors.accentSoft,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.cardBackgroundSecondary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(stop.transportType.iconRes),
                    contentDescription = stop.transportType.name,
                    colorFilter = ColorFilter.tint(AppColors.textPrimary),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stop.name.ifBlank { stringResource(Res.string.stop_with_index, index + 1) },
                    style = AppTypography.titleMedium.copy(fontSize = 15.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(Res.string.stop_with_index, index + 1),
                    style = AppTypography.labelSmall.copy(color = AppColors.textSecondary)
                )
            }

            IconButton(onClick = onDeleteStop) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.delete_stop),
                    tint = AppColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
