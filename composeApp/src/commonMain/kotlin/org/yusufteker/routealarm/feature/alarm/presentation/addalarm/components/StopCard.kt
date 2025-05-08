package org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.feature.alarm.domain.Stop

@Composable
fun StopCard(stop: Stop, index: Int, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.background(
            color = AppColors.cardBackground
        ).fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Durak $index",
                style = AppTypography.titleMedium,
                color = AppColors.textAlarmCard
            )
            Text(
                text = stop.name,
                style = AppTypography.bodyRegular,
                color = AppColors.textAlarmCard
            )

        }
    }
}
