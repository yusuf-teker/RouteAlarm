package org.yusufteker.routealarm.core.presentation.popup

import androidx.compose.runtime.Composable

sealed class PopupType {
    abstract val onDismiss: () -> Unit

    data class Info(
        val title: String,
        val message: String,
        override val onDismiss: () -> Unit = {}
    ) : PopupType()

    data class Confirm(
        val title: String,
        val message: String,
        val onConfirm: () -> Unit,
        override val onDismiss: () -> Unit = {}
    ) : PopupType()

    data class Error(
        val message: String,
        override val onDismiss: () -> Unit = {}
    ) : PopupType()

     class Custom(
        val content: @Composable (onDismiss: () -> Unit) -> Unit,
        override val onDismiss: () -> Unit = {}
    ) : PopupType()
}
