package org.yusufteker.routealarm

import androidx.compose.ui.window.ComposeUIViewController
import org.yusufteker.routealarm.app.App
import org.yusufteker.routealarm.di.initKoin

fun MainViewController() =
    ComposeUIViewController(
        configure = {
            initKoin()
        }
    ) { App() }