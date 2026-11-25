package org.yusufteker.routealarm.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.alarm_distance
import routealarm.composeapp.generated.resources.alarm_trigger_distance
import routealarm.composeapp.generated.resources.background_location_permission
import routealarm.composeapp.generated.resources.grant_permission
import routealarm.composeapp.generated.resources.location_permission
import routealarm.composeapp.generated.resources.notification_permission
import routealarm.composeapp.generated.resources.permission_granted
import routealarm.composeapp.generated.resources.permission_missing
import routealarm.composeapp.generated.resources.permissions_title
import routealarm.composeapp.generated.resources.settings
import routealarm.composeapp.generated.resources.sound_and_alerts
import routealarm.composeapp.generated.resources.stop_test_sound
import routealarm.composeapp.generated.resources.test_alarm_sound
import routealarm.composeapp.generated.resources.vibration
import routealarm.composeapp.generated.resources.vibration_desc
import kotlin.math.roundToInt

@Composable
fun SettingsScreenRoot(
    viewModel: SettingsViewModel = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SettingsScreen(
        state = state,
        onThresholdChange = viewModel::onThresholdChange,
        onVibrationChange = viewModel::onVibrationChange,
        onToggleTestSound = viewModel::onToggleTestSound,
        onRequestLocationPermission = viewModel::requestLocationPermission,
        onRequestBackgroundLocationPermission = viewModel::requestBackgroundLocationPermission,
        onRequestNotificationPermission = viewModel::requestNotificationPermission,
        modifier = Modifier
            .background(AppColors.background)
            .statusBarsPadding()
            .padding(bottom = contentPadding.calculateBottomPadding())
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onThresholdChange: (Int) -> Unit,
    onVibrationChange: (Boolean) -> Unit = {},
    onToggleTestSound: () -> Unit = {},
    onRequestLocationPermission: () -> Unit = {},
    onRequestBackgroundLocationPermission: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.settings),
            style = AppTypography.headlineLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )

        CarSliderInteractive(
            value = state.stopProximityThresholdMeters,
            onValueChange = onThresholdChange
        )

        // Sound & Alerts section
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.cardBackground,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(Res.string.sound_and_alerts),
                    style = AppTypography.titleMedium.copy(fontSize = 17.sp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AppColors.accentSoft,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.vibration),
                            style = AppTypography.bodyRegular.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = stringResource(Res.string.vibration_desc),
                            style = AppTypography.labelSmall.copy(color = AppColors.textSecondary)
                        )
                    }
                    Switch(
                        checked = state.isVibrationEnabled,
                        onCheckedChange = onVibrationChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.onAccent,
                            checkedTrackColor = AppColors.accent,
                            uncheckedThumbColor = AppColors.textSecondary,
                            uncheckedTrackColor = AppColors.cardBackgroundSecondary,
                            uncheckedBorderColor = AppColors.border
                        )
                    )
                }

                HorizontalDivider(color = AppColors.border.copy(alpha = 0.6f))

                OutlinedButton(
                    onClick = onToggleTestSound,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (state.isTestSoundPlaying) {
                            AppColors.danger.copy(alpha = 0.14f)
                        } else {
                            AppColors.surfaceElevated
                        },
                        contentColor = if (state.isTestSoundPlaying) {
                            AppColors.danger
                        } else {
                            AppColors.accentSoft
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (state.isTestSoundPlaying) {
                            Icons.Default.Close
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (state.isTestSoundPlaying) Res.string.stop_test_sound
                            else Res.string.test_alarm_sound
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Permissions status card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.cardBackground,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.permissions_title),
                    style = AppTypography.titleMedium.copy(fontSize = 17.sp)
                )

                PermissionStatusRow(
                    icon = Icons.Default.LocationOn,
                    title = stringResource(Res.string.location_permission),
                    isGranted = state.hasLocationPermission,
                    onGrantClick = onRequestLocationPermission
                )

                HorizontalDivider(color = AppColors.border.copy(alpha = 0.6f))

                PermissionStatusRow(
                    icon = Icons.Default.Place,
                    title = stringResource(Res.string.background_location_permission),
                    isGranted = state.hasBackgroundLocationPermission,
                    onGrantClick = onRequestBackgroundLocationPermission
                )

                HorizontalDivider(color = AppColors.border.copy(alpha = 0.6f))

                PermissionStatusRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(Res.string.notification_permission),
                    isGranted = state.hasNotificationPermission,
                    onGrantClick = onRequestNotificationPermission
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PermissionStatusRow(
    icon: ImageVector,
    title: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) AppColors.accentSoft else AppColors.warning,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.bodyRegular.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = stringResource(
                    if (isGranted) Res.string.permission_granted else Res.string.permission_missing
                ),
                style = AppTypography.labelSmall.copy(
                    color = if (isGranted) AppColors.accentSoft else AppColors.warning
                )
            )
        }
        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppColors.accent,
                modifier = Modifier.size(22.dp)
            )
        } else {
            OutlinedButton(
                onClick = onGrantClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AppColors.warning.copy(alpha = 0.12f),
                    contentColor = AppColors.warning
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.grant_permission),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarSliderInteractive(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 100..1000
) {
    val stepSize = 50
    val stepCount = ((valueRange.last - valueRange.first) / stepSize) - 1
    var sliderValue by remember(value) {
        mutableFloatStateOf(value.coerceIn(valueRange).toFloat())
    }
    val snappedValue = ((sliderValue / stepSize).roundToInt() * stepSize).coerceIn(valueRange)
    val valueFraction =
        ((snappedValue - valueRange.first).toFloat() / (valueRange.last - valueRange.first).toFloat())
            .coerceIn(0f, 1f)
    val presets = listOf(100, 250, 500, 750, 1000)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.cardBackground,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.border, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AppColors.accentSoft,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(Res.string.alarm_distance),
                            style = AppTypography.titleMedium.copy(fontSize = 17.sp)
                        )
                        Text(
                            text = stringResource(Res.string.alarm_trigger_distance, snappedValue),
                            style = AppTypography.labelSmall.copy(color = AppColors.textSecondary)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.accent.copy(alpha = 0.18f),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = AppColors.accent.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                ) {
                    Text(
                        text = if (snappedValue >= 1000) "1.0 km" else "$snappedValue m",
                        style = AppTypography.titleMedium.copy(
                            color = AppColors.accentSoft,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        val finalVal =
                            ((sliderValue / stepSize).roundToInt() * stepSize).coerceIn(valueRange)
                        sliderValue = finalVal.toFloat()
                        onValueChange(finalVal)
                    },
                    valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                    steps = stepCount.coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth(),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(AppColors.accent)
                                .border(2.5.dp, AppColors.onAccent, CircleShape)
                        )
                    },
                    track = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AppColors.surfaceElevated)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(valueFraction.coerceAtLeast(0.02f))
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(AppColors.accent, AppColors.accentSoft)
                                        )
                                    )
                            )
                        }
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.accent,
                        activeTrackColor = AppColors.accent,
                        inactiveTrackColor = AppColors.surfaceElevated
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${valueRange.first} m",
                        style = AppTypography.labelSmall.copy(color = AppColors.textSecondary)
                    )
                    Text(
                        text = "550 m",
                        style = AppTypography.labelSmall.copy(color = AppColors.textSecondary)
                    )
                    Text(
                        text = "1.0 km",
                        style = AppTypography.labelSmall.copy(color = AppColors.textSecondary)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = snappedValue == preset
                    val label = if (preset >= 1000) "1 km" else "$preset m"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) AppColors.accent.copy(alpha = 0.2f)
                                else AppColors.surfaceElevated
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AppColors.accent else AppColors.border,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                sliderValue = preset.toFloat()
                                onValueChange(preset)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = AppTypography.labelSmall.copy(
                                color = if (isSelected) AppColors.accentSoft else AppColors.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
