package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.yusufteker.routealarm.feature.location.domain.LocationUiModel

@Composable
expect fun PlatformMap(
    modifier: Modifier,
    selectedLocation: LocationUiModel?,
    onLocationSelected: (LocationUiModel) -> Unit
)
