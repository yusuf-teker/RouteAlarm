package org.yusufteker.routealarm.feature.location.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.feature.location.domain.Place
@Composable
fun LocationSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChanged: (String) -> Unit,
    suggestions: List<Place>,
    onSuggestionClicked: (Place) -> Unit
) {
    val hasSuggestions = suggestions.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            label = { Text("Konum Ara...") },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = AppColors.cardBackground, // Alt çizgi - focus
                cursorColor = AppColors.cardBackground,           // İmleç (cursor) rengi
                focusedLabelColor = AppColors.cardBackground,     // Label - focus
            )
        )

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
                    Text(
                        text = suggestion.name,
                        color = AppColors.textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionClicked(suggestion) }
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
