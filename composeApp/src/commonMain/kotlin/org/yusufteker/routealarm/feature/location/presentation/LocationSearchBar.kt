package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.UiText
import org.yusufteker.routealarm.feature.alarm.presentation.addstops.StopPickerAction
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.Place
import org.yusufteker.routealarm.feature.location.domain.distanceTextTo
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.map_pin
import routealarm.composeapp.generated.resources.search_location

@Composable
fun LocationSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChanged: (String) -> Unit,
    suggestions: List<Place>,
    onSuggestionClicked: (Place) -> Unit,
    currentLocation: Location? = null,
    onBackClick: () -> Unit = {  }
) {
    val hasSuggestions = suggestions.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {

        Row {
            IconButton( // BACK
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = AppColors.cardBackground,
                ),
                modifier = Modifier.size(48.dp),
                onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = AppColors.iconTint
                )
            }
            Spacer(Modifier.width(16.dp))
            TextField(
                value = query,
                onValueChange = onQueryChanged,
                label = { Text(
                    UiText.StringResourceId(Res.string.search_location).asString()
                ) },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = AppColors.cardBackground, // Alt çizgi - focus
                    cursorColor = AppColors.cardBackground,           // İmleç (cursor) rengi
                    focusedLabelColor = AppColors.cardBackground,     // Label - focus
                )
            )
        }


        AnimatedVisibility(
            visible = hasSuggestions
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .background(
                        color = AppColors.cardBackground,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                suggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        currentLocation?.let {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = modifier.weight(1f)) {
                                Image(
                                    painter = painterResource(Res.drawable.map_pin),
                                    contentDescription = "Map Pin",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )

                                Text(
                                    text = currentLocation.distanceTextTo(suggestion.latitude,suggestion.longitude),
                                    style = AppTypography.labelSmall,
                                    color = AppColors.textPrimary.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                    )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(5f)
                                .clickable { onSuggestionClicked(suggestion) }
                        ) {
                            Text(
                                text = suggestion.name,
                                style = AppTypography.bodyRegular,
                                color = AppColors.textPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 4.dp)
                            )
                            Text(
                                text = suggestion.address,
                                style = AppTypography.labelSmall,
                                color = AppColors.textPrimary.copy(alpha = 0.7f),
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, bottom = 8.dp, end = 12.dp)
                            )
                        }
                    }


                }
            }
        }
    }
}
