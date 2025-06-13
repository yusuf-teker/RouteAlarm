package org.yusufteker.routealarm

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.mp.KoinPlatform.getKoin
import org.yusufteker.routealarm.app.App
import org.yusufteker.routealarm.di.initKoin
import org.yusufteker.routealarm.permissions.IOSLocationPermissionsHandler
import org.yusufteker.routealarm.permissions.PermissionBridge


val LocalNativeViewFactory = staticCompositionLocalOf<NativeViewFactory> {
    error("NativeViewFactory not provided")
}

fun MainViewController(
    nativeViewFactory: NativeViewFactory
) =
    ComposeUIViewController(
        configure = {

            Napier.base(DebugAntilog()) // init logger

            //Koin
            initKoin()

            //Permission Handler
            val permissionBridge = getKoin().get<PermissionBridge>()
            permissionBridge.setListener(IOSLocationPermissionsHandler())
        }
    ) {
        CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory){
            App()
        }


    }