package org.yusufteker.routealarm.feature.alarm.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.yusufteker.routealarm.feature.alarm.domain.Alarm

@Composable
fun ActiveAlarm(alarm: Alarm){
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        StopProgressBar(alarm = alarm)

    }
}