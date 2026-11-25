package org.yusufteker.routealarm.permissions

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.core.presentation.AppColors
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.cancel
import routealarm.composeapp.generated.resources.notification_permission_message
import routealarm.composeapp.generated.resources.notification_permission_title
import routealarm.composeapp.generated.resources.open_settings

@Composable
fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onContinueClicked: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBackground,
        titleContentColor = AppColors.textPrimary,
        textContentColor = AppColors.textSecondary,
        shape = RoundedCornerShape(20.dp),
        title = { Text(stringResource(Res.string.notification_permission_title)) },
        text = { Text(stringResource(Res.string.notification_permission_message)) },
        confirmButton = {
            Button(
                onClick = onContinueClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.accent,
                    contentColor = AppColors.onAccent
                )
            ) {
                Text(stringResource(Res.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel), color = AppColors.textSecondary)
            }
        }
    )
}
