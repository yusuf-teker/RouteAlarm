package org.yusufteker.routealarm.feature.alarm.presentation.addalarm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.core.presentation.UiText
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.add_stop_button

@Composable
fun AddStopCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = UiText.StringResourceId(Res.string.add_stop_button).asString(), color = Color.Gray)
        }
    }
}
