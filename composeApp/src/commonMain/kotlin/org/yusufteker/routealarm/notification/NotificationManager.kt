package org.yusufteker.routealarm.notification


expect class NotificationManager {
    fun showNotification(
        title: String,
        description: String,
        onStopReached: () -> Unit = {}
    )
}