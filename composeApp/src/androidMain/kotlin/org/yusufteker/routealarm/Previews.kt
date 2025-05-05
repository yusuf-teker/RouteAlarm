package org.yusufteker.routealarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeScreen
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.StopProgressBar
import org.yusufteker.routealarm.feature.onboarding.presentation.welcome.WelcomeScreen

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen {}
}


@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(onAddAlarm = {}, onAlarmClick = {}, onSettingsClick = {})
}


@Preview(showBackground = true)
@Composable
fun StopProgressBarPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121212)) // Koyu arka plan
                .padding(16.dp)
        ) {

            StopProgressBar(
                title = "Başlık",
                passedStops = 1,
                stopNames = listOf("Start", "Dest1", "Dest2"),
                modifier = Modifier.background(
                    AppColors.cardBackground
                )
            )


        }
    }
}


