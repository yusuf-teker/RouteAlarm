package org.yusufteker.routealarm.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import org.yusufteker.routealarm.AndroidApplicationContext

actual fun openAppSettings() {
    val context = AndroidApplicationContext.appContext
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}