package org.yusufteker.routealarm.notification

import platform.Foundation.NSUUID
import platform.UserNotifications.*
import platform.darwin.NSObject

actual class NotificationManager {

    private var onStopReached: (() -> Unit)? = null

    private val notificationDelegate = object : NSObject(), UNUserNotificationCenterDelegateProtocol {
        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
        ) {
            withCompletionHandler(
                UNNotificationPresentationOptionAlert or
                        UNNotificationPresentationOptionBanner or
                        UNNotificationPresentationOptionSound or
                        UNNotificationPresentationOptionBadge
            )
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            didReceiveNotificationResponse: UNNotificationResponse,
            withCompletionHandler: () -> Unit
        ) {
            println("Action received: ${didReceiveNotificationResponse.actionIdentifier}")
            if (didReceiveNotificationResponse.actionIdentifier == "STOP_ACTION") {
                println("Durdur butonuna basıldı.")
                onStopReached?.invoke()
            }
            withCompletionHandler()
        }
    }

    init {
        UNUserNotificationCenter.currentNotificationCenter().delegate = notificationDelegate
    }

    actual fun showNotification(
        title: String,
        description: String,
        onStopReached: () -> Unit
    ) {
        this.onStopReached = onStopReached

        val center = UNUserNotificationCenter.currentNotificationCenter()

        val stopAction = UNNotificationAction.actionWithIdentifier(
            identifier = "STOP_ACTION",
            title = "Durdur",
            options = UNNotificationActionOptionForeground
        )

        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = "ALARM_CATEGORY",
            actions = listOf(stopAction),
            intentIdentifiers = listOf<String>(),
            options = UNNotificationCategoryOptionCustomDismissAction
        )

        center.setNotificationCategories(setOf(category))

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(description)
            setCategoryIdentifier("ALARM_CATEGORY")
            setSound(UNNotificationSound.defaultSound())
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = 0.1,
            repeats = false
        )

        val uuid = NSUUID.UUID().UUIDString()
        val request = UNNotificationRequest.requestWithIdentifier(uuid, content, trigger)

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Notification Error: $error")
            } else {
                println("Notification scheduled successfully")
            }
        }
    }
}
