package org.yusufteker.routealarm.permissions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onContinueClicked: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bildirim izni gerekli") },
        text = { Text("Uygulama, alarm bildirimleri gösterebilmek için bildirim iznine ihtiyaç duyuyor.") },
        confirmButton = {
            TextButton(onClick = onContinueClicked) {
                Text("Ayarları Aç")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
