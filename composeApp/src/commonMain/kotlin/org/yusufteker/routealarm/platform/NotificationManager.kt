package org.yusufteker.routealarm.platform


expect class NotificationManager {
    fun showNotification(
        title: String,
        description: String,
        onStopReached: () -> Unit = {}
    )
}