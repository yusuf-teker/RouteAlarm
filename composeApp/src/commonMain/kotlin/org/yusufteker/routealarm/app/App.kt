package org.yusufteker.routealarm.app

import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.yusufteker.routealarm.core.popup.GlobalPopupHost
import org.yusufteker.routealarm.core.popup.LocalPopupManager
import org.yusufteker.routealarm.core.popup.PopupManager

@Composable
@Preview
fun App() {

    val popupManager = koinInject<PopupManager>()

    CompositionLocalProvider(
        LocalPopupManager provides popupManager
    ) {
        val navController = rememberNavController()

        AppNavHost(navController = navController)
        GlobalPopupHost()
    }

}