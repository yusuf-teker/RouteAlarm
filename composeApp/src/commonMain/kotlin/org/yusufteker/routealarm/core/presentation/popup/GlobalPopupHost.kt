package org.yusufteker.routealarm.core.presentation.popup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.UiText
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.cancel
import routealarm.composeapp.generated.resources.close
import routealarm.composeapp.generated.resources.confirm
import routealarm.composeapp.generated.resources.destination_reached
import routealarm.composeapp.generated.resources.error
import routealarm.composeapp.generated.resources.ok

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
                title = UiText.StringResourceId(popup.title).asString() ,
                message = UiText.StringResourceId(popup.message).asString(),
                onConfirm = {
                    popup.onConfirm()
                    popupManager.dismissPopup(popup)
                },
                onDismiss = { popupManager.dismissPopup(popup) }
            )

            is PopupType.Error -> ErrorPopup(
                message = UiText.StringResourceId(popup.message).asString() ,
                onDismiss = { popupManager.dismissPopup(popup) }
            )

            is PopupType.Custom -> CustomPopup(
                content = popup.content,
                onDismiss = {
                    popupManager.dismissPopup(popup)
                    Napier.d ("CustomPopup global popup onDismiss", tag = "popup")
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
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
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
        title = { Text(text = title) },
        text = { Text(text = message) },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(UiText.StringResourceId(Res.string.cancel).asString())
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
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
        title = { Text(UiText.StringResourceId(Res.string.error).asString()) },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
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
            .background(Color(0x80000000))
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
fun GoalReachedPopup(onDismiss: () -> Unit) {

    Column(
        modifier = Modifier
            .width(300.dp)
            .background(Color.White, shape = RoundedCornerShape(
                size = 16.dp
            )).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            UiText.StringResourceId(Res.string.destination_reached).asString(), style = AppTypography.titleLarge.copy(
            color = Color.Black
        ))
        Spacer(Modifier.height(16.dp))

        val composition by rememberLottieComposition {
            LottieCompositionSpec.JsonString(Res.readBytes( "files/location_reached.json").decodeToString())
        }
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = Compottie.IterateForever
        )
        Image(rememberLottiePainter(
            composition = composition,
            progress = { progress }),null, modifier = Modifier
            .size(200.dp),
            alignment = Alignment.Center, contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(16.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(UiText.StringResourceId(Res.string.close).asString())
        }
    }
}




