package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.Place
import org.yusufteker.routealarm.feature.location.domain.distanceTextTo
import org.yusufteker.routealarm.feature.location.domain.emptyLocation
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.clear_search
import routealarm.composeapp.generated.resources.go_back
import routealarm.composeapp.generated.resources.map_pin
import routealarm.composeapp.generated.resources.no_locations_found
import routealarm.composeapp.generated.resources.search_location
import routealarm.composeapp.generated.resources.searching_locations

@Composable
fun LocationSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChanged: (String) -> Unit,
    suggestions: List<Place>,
    onSuggestionClicked: (Place) -> Unit,
    currentLocation: Location? = null,
    isSearching: Boolean = false,
    hasSearched: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val hasSuggestions = suggestions.isNotEmpty()
    val showStatusPanel = query.isNotBlank() && !hasSuggestions && (isSearching || hasSearched)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.cardBackground.copy(alpha = 0.94f))
                    .border(1.dp, AppColors.border, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.go_back),
                    tint = AppColors.textPrimary
                )
            }

            Spacer(Modifier.width(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                placeholder = {
                    Text(
                        text = stringResource(Res.string.search_location),
                        color = AppColors.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AppColors.accentSoft
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.clear_search),
                                tint = AppColors.textSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.cardBackground.copy(alpha = 0.95f),
                    unfocusedContainerColor = AppColors.cardBackground.copy(alpha = 0.92f),
                    focusedBorderColor = AppColors.accent,
                    unfocusedBorderColor = AppColors.border,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    cursorColor = AppColors.accent
                ),
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(visible = hasSuggestions) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.cardBackground.copy(alpha = 0.97f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                ) {
                    itemsIndexed(suggestions) { index, suggestion ->
                        val hasCoords =
                            suggestion.latitude != emptyLocation.lat || suggestion.longitude != emptyLocation.lng
                        val showDistance =
                            hasCoords &&
                                currentLocation != null &&
                                (currentLocation.lat != emptyLocation.lat || currentLocation.lng != emptyLocation.lng)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionClicked(suggestion) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(56.dp)
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.map_pin),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(AppColors.accent)
                                )
                                if (showDistance && currentLocation != null) {
                                    Text(
                                        text = currentLocation.distanceTextTo(
                                            suggestion.latitude,
                                            suggestion.longitude
                                        ),
                                        style = AppTypography.labelSmall.copy(
                                            color = AppColors.accentSoft
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = suggestion.name,
                                    style = AppTypography.bodyRegular.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (suggestion.address.isNotBlank()) {
                                    Text(
                                        text = suggestion.address,
                                        style = AppTypography.labelSmall.copy(
                                            color = AppColors.textSecondary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (index < suggestions.lastIndex) {
                            HorizontalDivider(color = AppColors.border.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showStatusPanel) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AppColors.cardBackground.copy(alpha = 0.96f),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .border(1.dp, AppColors.border, RoundedCornerShape(14.dp))
            ) {
                Text(
                    text = if (isSearching) {
                        stringResource(Res.string.searching_locations)
                    } else {
                        stringResource(Res.string.no_locations_found)
                    },
                    style = AppTypography.labelSmall.copy(color = AppColors.textSecondary),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
