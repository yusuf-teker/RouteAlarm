package org.yusufteker.routealarm

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.mp.KoinPlatform.getKoin
import org.yusufteker.routealarm.app.App
import org.yusufteker.routealarm.di.initKoin
import org.yusufteker.routealarm.permissions.IOSLocationPermissionsHandler
import org.yusufteker.routealarm.permissions.PermissionBridge


fun MainViewController() =
    ComposeUIViewController(
        configure = {

            //Koin
            initKoin()

            //Permission Handler
            val permissionBridge = getKoin().get<PermissionBridge>()
            permissionBridge.setListener(IOSLocationPermissionsHandler())
        }
    ) {
        App()

    }