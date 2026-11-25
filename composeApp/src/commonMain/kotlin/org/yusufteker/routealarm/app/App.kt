package org.yusufteker.routealarm.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.yusufteker.routealarm.core.presentation.RouteAlarmTheme
import org.yusufteker.routealarm.core.presentation.popup.GlobalPopupHost
import org.yusufteker.routealarm.core.presentation.popup.LocalPopupManager
import org.yusufteker.routealarm.core.presentation.popup.PopupManager

@Composable
@Preview
fun App() {
    val popupManager = koinInject<PopupManager>()

    RouteAlarmTheme {
        CompositionLocalProvider(
            LocalPopupManager provides popupManager
        ) {
            val navController = rememberNavController()

            AppNavHost(navController = navController)
            GlobalPopupHost()
        }
    }
}
