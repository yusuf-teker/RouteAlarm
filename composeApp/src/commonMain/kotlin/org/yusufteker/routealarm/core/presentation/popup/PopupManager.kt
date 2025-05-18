package org.yusufteker.routealarm.core.presentation.popup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf

class PopupManager {
    private val _popups = mutableStateListOf<PopupType>()
    val popups: List<PopupType> get() = _popups

    fun showPopup(popup: PopupType) {
        _popups.add(popup)
    }

    fun dismissPopup(popup: PopupType) {
        _popups.remove(popup)
        popup.onDismiss()
    }

    fun showInfo(title: String, message: String, onDismiss: () -> Unit = {}) {
        showPopup(PopupType.Info(title, message, onDismiss))
    }

    fun showConfirm(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        showPopup(PopupType.Confirm(title, message, onConfirm, onDismiss))
    }

    fun showError(message: String, onDismiss: () -> Unit = {}) {
        showPopup(PopupType.Error(message, onDismiss))
    }

    fun showCustom(content: @Composable (onDismiss: () -> Unit) -> Unit, onDismiss: () -> Unit = {}) {
        showPopup(PopupType.Custom(content, onDismiss))
    }
}


