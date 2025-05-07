package org.yusufteker.routealarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.data.dummy.fakeAlarms
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeScreen
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeState
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.BottomNavigationBar
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
    HomeScreen(state = HomeState(), {})
}


@Preview(showBackground = true)
@Composable
fun StopProgressBarPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            StopProgressBar(
                fakeAlarms.get(0),
                modifier = Modifier.background(
                    AppColors.cardBackground
                )
            )


        }

    }

}
@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    var selectedRoute by remember { mutableStateOf(Routes.HomeScreen.toString()) }

    BottomNavigationBar(
        currentRoute = selectedRoute,
        onItemSelected = { selectedRoute = it.toString() }
    )
}



