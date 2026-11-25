package org.yusufteker.routealarm.core.presentation

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppTypography {

    val headlineLarge = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = AppColors.textPrimary,
        letterSpacing = (-0.5).sp
    )

    val titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = AppColors.textPrimary
    )

    val titleMedium = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.textPrimary
    )

    val bodyRegular = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        color = AppColors.textPrimary
    )

    val labelSmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.textSecondary
    )
}
