package org.yusufteker.routealarm

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.getKoin
import org.yusufteker.routealarm.app.App
import org.yusufteker.routealarm.permissions.AndroidLocationPermissionsHandler
import org.yusufteker.routealarm.permissions.PermissionBridge

class MainActivity : ComponentActivity() {
    private lateinit var permissionBridge: PermissionBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        permissionBridge = getKoin().get()
        permissionBridge.setListener(
            AndroidLocationPermissionsHandler(this, permissionBridge)
        )

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::permissionBridge.isInitialized) {
            permissionBridge.notifyAppResumed()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
