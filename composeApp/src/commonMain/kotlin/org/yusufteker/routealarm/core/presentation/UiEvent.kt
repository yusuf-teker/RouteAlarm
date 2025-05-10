package org.yusufteker.routealarm.core.presentation

sealed class UiEvent {
    object NavigateBack : UiEvent()
    data class ShowError(val message: String) : UiEvent()
    data class ShowInfo(val title: String, val message: String) : UiEvent()
    data class ShowConfirm(
        val title: String,
        val message: String,
        val onConfirm: () -> Unit
    ) : UiEvent()
    // Daha fazla event türü eklenebilir.
}