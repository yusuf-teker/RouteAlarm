package org.yusufteker.routealarm.feature.alarm.presentation.addstops.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.feature.alarm.domain.TransportType
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.transport_type_label

@Composable
fun TransportTypeChipRow(
    selected: TransportType,
    onSelected: (TransportType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = TransportType.entries.filterNot { it == TransportType.DEFAULT }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.transport_type_label),
            style = AppTypography.labelSmall.copy(
                color = AppColors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(types, key = { it.name }) { type ->
                val isSelected = type == selected
                val label = stringResource(type.labelRes)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) AppColors.accent.copy(alpha = 0.2f)
                            else AppColors.surfaceElevated
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) AppColors.accent else AppColors.border,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelected(type) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        painter = painterResource(type.iconRes),
                        contentDescription = label,
                        colorFilter = ColorFilter.tint(
                            if (isSelected) AppColors.accentSoft else AppColors.textSecondary
                        ),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = label,
                        style = AppTypography.labelSmall.copy(
                            color = if (isSelected) AppColors.accentSoft else AppColors.textPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TransportTypeDropdownSelector(
    modifier: Modifier = Modifier,
    selected: TransportType,
    onSelected: (TransportType) -> Unit
) {
    TransportTypeChipRow(
        selected = selected,
        onSelected = onSelected,
        modifier = modifier
    )
}
