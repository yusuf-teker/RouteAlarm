package org.yusufteker.routealarm.core.presentation

import org.yusufteker.routealarm.app.Routes

sealed class UiEvent {
    object NavigateBack : UiEvent()
    data class NavigateTo(val route: Routes) : UiEvent()
    data class NavigateWithData<T>(val route: Routes, val data: T) : UiEvent()

}