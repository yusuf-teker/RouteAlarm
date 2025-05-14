package org.yusufteker.routealarm

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import org.yusufteker.routealarm.app.App
import org.koin.android.ext.android.getKoin
import org.yusufteker.routealarm.permissions.AndroidLocationPermissionsHandler
import org.yusufteker.routealarm.permissions.PermissionBridge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        val permissionBridge = getKoin().get<PermissionBridge>()
        permissionBridge.setListener(
            AndroidLocationPermissionsHandler(this, permissionBridge)
        )

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}