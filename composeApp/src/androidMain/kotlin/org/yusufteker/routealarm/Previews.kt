package org.yusufteker.routealarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.data.dummy.fakeAlarms
import org.yusufteker.routealarm.core.data.dummy.fakeStops
import org.yusufteker.routealarm.core.presentation.button.PrimaryButton
import org.yusufteker.routealarm.core.presentation.card.AdaptiveCard
import org.yusufteker.routealarm.core.presentation.card.CardContent
import org.yusufteker.routealarm.core.presentation.modifier.rainbowColors
import org.yusufteker.routealarm.core.presentation.modifier.rgbBorder

import org.yusufteker.routealarm.core.presentation.popup.GoalReachedPopup
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmScreen
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmState
import org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail.AlarmDetailScreen
import org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail.AlarmDetailState
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeScreen
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeState
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.ActiveAlarm
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.BottomNavigationBar
import org.yusufteker.routealarm.feature.location.domain.Place
import org.yusufteker.routealarm.feature.location.presentation.LocationSearchBar
import org.yusufteker.routealarm.feature.onboarding.presentation.welcome.WelcomeScreen
import org.yusufteker.routealarm.permissions.LocationPermissionDialog
import org.yusufteker.routealarm.settings.CarSliderInteractive

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
            modifier = Modifier.fillMaxWidth()
        ) {

            ActiveAlarm(
                fakeAlarms.get(0)
            )


        }

    }

}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    var selectedRoute by remember { mutableStateOf(Routes.HomeScreen.toString()) }

    BottomNavigationBar(
        currentRoute = selectedRoute, onItemSelected = { selectedRoute = it.toString() })
}

@Preview(showBackground = true)
@Composable
fun AddAlarmScreenPreview() {

    val sampleState = AddAlarmState(
        title = "İşe Gidiş Alarmı", stops = fakeStops
    )

    AddAlarmScreen(
        state = sampleState, onAction = {}, contentPadding = PaddingValues()
    )
}


@Preview(showBackground = true)
@Composable
fun AdaptiveCardPrev() {
    var text by remember { mutableStateOf("Deneme") }

    Column(Modifier.background(Color.Transparent)) {
        AdaptiveCard(
            content = CardContent.EditableTextContent(
                value = text, onValueChange = { text = it }, placeholder = "Enter your text"
            )
        )
        AdaptiveCard(
            content = CardContent.TextContent(
                text = "Text",

                )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrimaryButtonPrev() {
    PrimaryButton(
        text = "Get started", onClick = { /* Handle click */ })
}

@Preview
@Composable
fun LocationSearchBarPreview() {
    LocationSearchBar(
        query = "Search location...",
        onQueryChanged = {},
        suggestions = listOf(
            Place("1", "Suggestion 1", "", 1.1, 1.1),
            Place("2", "Suggestion 2", "", 1.1, 1.1),
            Place("3", "Suggestion 3", "", 1.1, 1.1),
            Place("4", "Suggestion 4", "", 1.1, 1.1),
            Place("5", "Suggestion 5", "", 1.1, 1.1)
        ),
        onSuggestionClicked = {}
    )

}

@Preview
@Composable
fun LocationPermissionDialogPrew(){
    LocationPermissionDialog(
        onDismiss = {},
        onContinueClicked = {}
    )
}

@Preview
@Composable
fun GoalReachedPopupPreview() {
    GoalReachedPopup(onDismiss = {})
}


@Preview
@Composable
fun CarSliderInteractivePrew(){
    CarSliderInteractive(
        value = 100,
        onValueChange = {}
    )
}

@Preview
@Composable
fun AlarmDetailScreenPrew(){
    AlarmDetailScreen(
        state = AlarmDetailState(
            alarm = fakeAlarms.get(0),
            isAlarmActive = true,
            previousStops = fakeStops.take(2),
            nextStops = fakeStops.drop(4),
            progress = 0.5f,
            remainingDistance = 1000.0F
        ),
        onAction = {},
        contentPadding = PaddingValues()
    )
}

@Preview
@Composable
fun RainbowBorderPreview() {
    Column(Modifier.fillMaxSize()) {

        Text(modifier = Modifier.rgbBorder(
            strokeWidth = 8.dp,
            shape = RectangleShape,
            duration = 5000,
            colorList = rainbowColors
        ).size(600.dp).padding(16.dp),
            text = "RGB Border Example")

        Box(
            modifier = Modifier.fillMaxSize()
                .background(Color.Black)
                .rgbBorder(strokeWidth = 8.dp, shape = CircleShape, duration = 5000)
        )



    }

}
