package org.yusufteker.routealarm

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeScreen
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


