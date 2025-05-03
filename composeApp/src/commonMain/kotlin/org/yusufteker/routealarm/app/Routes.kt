package org.yusufteker.routealarm.app

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes {

    // Giriş akışı
    @Serializable
    data object WelcomeScreen : Routes

    @Serializable
    data object AuthScreen : Routes // Sign Up / Login (şimdilik kullanılmayacak)

    // Ana ekran ve yönlendirmeler
    @Serializable
    data object HomeScreen : Routes

    @Serializable
    data class AlarmDetailScreen(val alarmId: String) : Routes

    @Serializable
    data object AddAlarmScreen : Routes

    @Serializable
    data class StopPickerScreen(val alarmId: String) : Routes

    @Serializable
    data class ActiveAlarmScreen(val alarmId: String) : Routes

    @Serializable
    data object SettingsScreen : Routes
}