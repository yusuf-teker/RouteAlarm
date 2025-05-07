package org.yusufteker.routealarm.core.presentation

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppTypography {

    val titleLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = AppColors.textPrimary
    )

    val titleMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.textPrimary
    )

    val bodyRegular = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = AppColors.textPrimary
    )

    val labelSmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        color = AppColors.textPrimary
    )
}
