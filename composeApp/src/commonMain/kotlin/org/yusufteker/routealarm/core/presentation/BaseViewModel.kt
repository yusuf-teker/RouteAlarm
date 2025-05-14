package org.yusufteker.routealarm.core.presentation


import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.yusufteker.routealarm.core.presentation.popup.PopupManager

import org.koin.core.component.inject
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.popup.PopupType

open class BaseViewModel : ViewModel(), KoinComponent {
    // PopupManager'ı Koin ile inject ediyoruz
    protected val popupManager: PopupManager by inject()

    // Popup gösterme metodları
    fun showInfoPopup(title: String, message: String) {
        popupManager.showPopup(PopupType.Info(title, message))
    }

    fun showConfirmPopup(title: String, message: String, onConfirm: () -> Unit) {
        popupManager.showPopup(PopupType.Confirm(title, message, onConfirm))
    }

    fun showErrorPopup(message: String) {
        popupManager.showPopup(PopupType.Error(message))
    }

    fun showCustomPopup(content: @Composable () -> Unit) {
        popupManager.showPopup(PopupType.Custom(content))
    }

    val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    suspend fun sendUiEvent(event: UiEvent) {
        _uiEvent.emit(event)
    }

    fun sendUiEventSafe(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    fun navigateTo(route: Routes) {
        sendUiEventSafe(UiEvent.NavigateTo(route))
    }

    fun navigateBack() {
        sendUiEventSafe(UiEvent.NavigateBack)
    }
    fun showSuccessAndNavigate(message: String, route: Routes) {
        showInfoPopup("Başarılı", message)
        navigateTo(route)
    }
}
