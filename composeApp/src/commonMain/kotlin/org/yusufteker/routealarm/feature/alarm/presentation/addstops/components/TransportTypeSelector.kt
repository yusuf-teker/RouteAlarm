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
    modifier: Modifier = Modifier,
    selected: TransportType,
    onSelected: (TransportType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(AppColors.cardBackground, RoundedCornerShape(8.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(52.dp)
        ) {
            Image(
                painter = painterResource(selected.iconRes),
                contentDescription = selected.name,
                modifier = Modifier.size(48.dp)
            )
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentHeight()
                        .width(200.dp)
                        .padding(top = 8.dp),
                    tonalElevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.cardBackground
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        val types = TransportType.entries.filterNot { it == TransportType.DEFAULT }

                        // 3’lü grid gibi diz
                        types.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { type ->
                                    IconButton(
                                        onClick = {
                                            onSelected(type)
                                            expanded = false
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Image(
                                            painter = painterResource(type.iconRes),
                                            contentDescription = type.name,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

