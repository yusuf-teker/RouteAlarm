package org.yusufteker.routealarm.permissions


import platform.UIKit.UIApplication
import platform.Foundation.NSURL
import platform.UIKit.UIApplicationOpenSettingsURLString


actual fun openAppSettings() {
    val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    val application = UIApplication.sharedApplication ?: return

    if (application.canOpenURL(settingsUrl)) {
        application.openURL(settingsUrl, emptyMap<Any?, Any>()) { success ->
            if (!success) {
                println("Failed to open settings")
            }
        }
    }
}