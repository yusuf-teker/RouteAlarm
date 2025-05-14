package org.yusufteker.routealarm.core.presentation

import org.yusufteker.routealarm.app.Routes

sealed class UiEvent {
    object NavigateBack : UiEvent()
    data class ShowError(val message: String) : UiEvent()
    data class ShowInfo(val title: String, val message: String) : UiEvent()
    data class ShowConfirm(
        val title: String,
        val message: String,
        val onConfirm: () -> Unit
    ) : UiEvent()
    data class NavigateTo(val route: Routes) : UiEvent()
    data class NavigateWithData<T>(val route: Routes, val data: T) : UiEvent()

    // Daha fazla event türü eklenebilir.
}