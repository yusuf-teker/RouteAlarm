package org.yusufteker.routealarm.feature.alarm.presentation.addstops.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.yusufteker.routealarm.feature.alarm.domain.TransportType

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.yusufteker.routealarm.core.presentation.AppColors

@Composable
fun TransportTypeDropdownSelector(
    modifier: Modifier = Modifier, selected: TransportType, onSelected: (TransportType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.background(
            color = AppColors.cardBackground,
            shape = RoundedCornerShape(4.dp),
        ), contentAlignment = Alignment.Center
    ) {

        // Ana ikon (her zaman görünen)
        IconButton(
            onClick = { expanded = true }, modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(selected.iconRes),
                contentDescription = selected.name,
                modifier = Modifier.fillMaxSize().padding(4.dp)
            )
        }

        // Açılır liste (Popup)
        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier.wrapContentHeight().width(100.dp).padding(top = 8.dp),
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        TransportType.entries.filterNot { it == TransportType.DEFAULT }
                            .forEach { type ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                            onSelected(type)
                                            expanded = false
                                        }.padding(4.dp)) {
                                    Image(
                                        painter = painterResource(type.iconRes),
                                        contentDescription = type.name,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = type.name.lowercase()
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}
