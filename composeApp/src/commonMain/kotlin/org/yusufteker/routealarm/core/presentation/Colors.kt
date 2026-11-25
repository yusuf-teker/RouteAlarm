package org.yusufteker.routealarm.core.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WelcomeBackgroundColor = Color(0xFF0B1622)

object AppColors {
    val background = Color(0xFF0B1622)
    val surfaceElevated = Color(0xFF132336)
    val cardBackground = Color(0xFF15273C)
    val cardBackgroundSecondary = Color(0xFF1E3550)
    val activeAlarmBarBackground = Color(0xFF0F1F31)

    val accent = Color(0xFF10B981)
    val accentSoft = Color(0xFF34D399)
    val onAccent = Color(0xFF042F2E)
    val darkGreen = Color(0xFF065F46)

    val buttonBackground = Color(0xFF10B981)
    val buttonText = Color(0xFF042F2E)

    val textPrimary = Color(0xFFF8FAFC)
    val textSecondary = Color(0xFF94A3B8)
    val textAlarmCard = Color(0xFFCBD5E1)

    val border = Color(0xFF233A54)

    val green = Color(0xFF10B981)
    val red = Color(0xFFEF4444)
    val danger = Color(0xFFEF4444)
    val warning = Color(0xFFF59E0B)

    val iconTint = Color(0xFFF8FAFC)

    val nightGradientReversed = listOf(
        Color(0xFF15273C),
        Color(0xFF1B334E),
        Color(0xFF15273C)
    )
}

private val RouteAlarmDarkColorScheme = darkColorScheme(
    primary = AppColors.accent,
    onPrimary = AppColors.onAccent,
    secondary = AppColors.accentSoft,
    onSecondary = AppColors.onAccent,
    background = AppColors.background,
    onBackground = AppColors.textPrimary,
    surface = AppColors.cardBackground,
    onSurface = AppColors.textPrimary,
    surfaceVariant = AppColors.cardBackgroundSecondary,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.border,
    error = AppColors.danger,
    onError = Color.White
)

@Composable
fun RouteAlarmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RouteAlarmDarkColorScheme,
        content = content
    )
}
