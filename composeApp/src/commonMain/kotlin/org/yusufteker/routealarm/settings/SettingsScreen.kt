package org.yusufteker.routealarm.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.bus_stop
import routealarm.composeapp.generated.resources.map_pin
import routealarm.composeapp.generated.resources.minibus

@Composable
fun SettingsScreenRoot(
    viewModel: SettingsViewModel = koinViewModel(),
    contentPadding : PaddingValues = PaddingValues(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onThresholdChange = { viewModel.onThresholdChange(it) },
        Modifier.background(
            AppColors.background
        ).padding(contentPadding)
    )
}
@Composable
fun SettingsScreen(
    state: SettingsState,
    onThresholdChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ayarlar", style = AppTypography.titleLarge)

        CarSliderInteractive(
            value = state.stopProximityThresholdMeters,
            onValueChange = onThresholdChange
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarSliderInteractive(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier.background(AppColors.cardBackground, shape = RoundedCornerShape(16.dp)),
    valueRange: IntRange = 100..1000
) {
    val clampedValue = value.coerceIn(valueRange)
    val valueFraction = (clampedValue - valueRange.first).toFloat() / (valueRange.last - valueRange.first).toFloat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Alarm Mesafesi",
            style = AppTypography.titleMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Image(
                painter = painterResource(resource = Res.drawable.bus_stop),
                contentDescription = "Hedef",
                modifier = Modifier.size(64.dp).align(Alignment.Bottom)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val fullWidth: Dp = maxWidth
                val carIconWidth = 48.dp
                val travelableWidth = fullWidth - carIconWidth

                // Yol görünümü
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp), // kenarlardan boşluk
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(25) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height(2.dp)
                                    .background(Color.White, shape = RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }

                // Araba ikonu, valueFraction'a göre konumlandırılıyor
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = travelableWidth * valueFraction)
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.minibus),
                        contentDescription = "Araba",
                        modifier = Modifier
                            .size(48.dp)
                            .scale(scaleX = -1f, scaleY = 1f),
                        tint = AppColors.textPrimary
                    )
                }

                // Görünmeyen Slider
                Slider(
                    value = clampedValue.toFloat(),
                    onValueChange = { onValueChange(it.toInt()) },
                    valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                    steps = valueRange.last - valueRange.first - 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    thumb = {}, // görünmeyen thumb
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "$clampedValue metre kala alarm çalacak",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textPrimary
        )
    }
}


