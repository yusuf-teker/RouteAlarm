package org.yusufteker.routealarm.core.popup

import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

@Composable
fun GlobalPopupHost() {
    val popupManager = LocalPopupManager.current
    val popups = popupManager.popups

    popups.forEach { popup ->
        when (popup) {
            is PopupType.Info -> InfoPopup(
                title = popup.title,
                message = popup.message,
                onDismiss = { popupManager.dismissPopup(popup) }
            )

            is PopupType.Confirm -> ConfirmPopup(
                title = popup.title,
                message = popup.message,
                onConfirm = {
                    popup.onConfirm()
                    popupManager.dismissPopup(popup)
                },
                onDismiss = { popupManager.dismissPopup(popup) }
            )

            is PopupType.Error -> ErrorPopup(
                message = popup.message,
                onDismiss = { popupManager.dismissPopup(popup) }
            )

            is PopupType.Custom -> CustomPopup(
                content = popup.content,
                onDismiss = { popupManager.dismissPopup(popup) }
            )
        }
    }
}



@Composable
private fun InfoPopup(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}

@Composable
private fun ConfirmPopup(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text("Onayla")
            }
        }
    )
}

@Composable
private fun ErrorPopup(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hata") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}

@Composable
private fun CustomPopup(
    content: @Composable () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {},
        text = content,
        confirmButton = {}
    )
}