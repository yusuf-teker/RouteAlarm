package org.yusufteker.routealarm.core.presentation.popup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.UiText
import org.yusufteker.routealarm.core.presentation.button.PrimaryButton
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.cancel
import routealarm.composeapp.generated.resources.close
import routealarm.composeapp.generated.resources.confirm
import routealarm.composeapp.generated.resources.destination_reached
import routealarm.composeapp.generated.resources.error
import routealarm.composeapp.generated.resources.ok
import routealarm.composeapp.generated.resources.stop_reached_message

@Composable
fun GlobalPopupHost() {
    val popupManager = LocalPopupManager.current
    val popups = popupManager.popups

    popups.forEach { popup ->
        when (popup) {
            is PopupType.Info -> InfoPopup(
                title = UiText.StringResourceId(popup.title).asString(),
                message = UiText.StringResourceId(popup.message).asString(),
                onDismiss = { popupManager.dismissPopup(popup) }
            )

            is PopupType.Confirm -> ConfirmPopup(
                title = UiText.StringResourceId(popup.title).asString(),
                message = UiText.StringResourceId(popup.message).asString(),
                onConfirm = {
                    popup.onConfirm()
                    popupManager.dismissPopup(popup)
                },
                onDismiss = { popupManager.dismissPopup(popup) }
            )

            is PopupType.Error -> ErrorPopup(
                message = UiText.StringResourceId(popup.message).asString(),
                onDismiss = { popupManager.dismissPopup(popup) }
            )

            is PopupType.Custom -> CustomPopup(
                content = popup.content,
                onDismiss = {
                    popupManager.dismissPopup(popup)
                }
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
        containerColor = AppColors.cardBackground,
        titleContentColor = AppColors.textPrimary,
        textContentColor = AppColors.textSecondary,
        shape = RoundedCornerShape(20.dp),
        title = { Text(text = title, style = AppTypography.titleMedium) },
        text = { Text(text = message, style = AppTypography.bodyRegular, color = AppColors.textSecondary) },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.accent,
                    contentColor = AppColors.onAccent
                )
            ) {
                Text(UiText.StringResourceId(Res.string.ok).asString())
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
        containerColor = AppColors.cardBackground,
        titleContentColor = AppColors.textPrimary,
        textContentColor = AppColors.textSecondary,
        shape = RoundedCornerShape(20.dp),
        title = { Text(text = title, style = AppTypography.titleMedium) },
        text = { Text(text = message, style = AppTypography.bodyRegular, color = AppColors.textSecondary) },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(UiText.StringResourceId(Res.string.cancel).asString(), color = AppColors.textSecondary)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.accent,
                    contentColor = AppColors.onAccent
                )
            ) {
                Text(UiText.StringResourceId(Res.string.confirm).asString())
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
        containerColor = AppColors.cardBackground,
        titleContentColor = AppColors.danger,
        textContentColor = AppColors.textSecondary,
        shape = RoundedCornerShape(20.dp),
        title = { Text(UiText.StringResourceId(Res.string.error).asString(), style = AppTypography.titleMedium, color = AppColors.danger) },
        text = { Text(text = message, style = AppTypography.bodyRegular, color = AppColors.textSecondary) },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.accent,
                    contentColor = AppColors.onAccent
                )
            ) {
                Text(UiText.StringResourceId(Res.string.ok).asString())
            }
        }
    )
}

@Composable
fun CustomPopup(
    content: @Composable (onDismiss: () -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        content(onDismiss)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GoalReachedPopup(
    onDismiss: () -> Unit,
    stopName: String = ""
) {
    Column(
        modifier = Modifier
            .width(320.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.cardBackground)
            .border(1.dp, AppColors.accent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {}
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = UiText.StringResourceId(Res.string.destination_reached).asString(),
            style = AppTypography.titleLarge,
            color = AppColors.accent,
            textAlign = TextAlign.Center
        )

        if (stopName.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(Res.string.stop_reached_message, stopName),
                style = AppTypography.bodyRegular,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(16.dp))

        val composition by rememberLottieComposition {
            LottieCompositionSpec.JsonString(Res.readBytes("files/location_reached.json").decodeToString())
        }
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = Compottie.IterateForever
        )
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                progress = { progress }
            ),
            contentDescription = null,
            modifier = Modifier.size(160.dp),
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(20.dp))

        PrimaryButton(
            text = UiText.StringResourceId(Res.string.close).asString(),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
