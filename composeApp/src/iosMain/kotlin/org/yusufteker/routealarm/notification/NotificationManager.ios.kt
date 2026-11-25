package org.yusufteker.routealarm.notification

import platform.Foundation.NSUUID
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionForeground
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionCustomDismissAction
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual class NotificationManager {

    private var onStopReached: (() -> Unit)? = null

    private val notificationDelegate = object : NSObject(), UNUserNotificationCenterDelegateProtocol {
        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
        ) {
            withCompletionHandler(
                UNNotificationPresentationOptionBanner or
                    UNNotificationPresentationOptionList or
                    UNNotificationPresentationOptionSound
            )
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            didReceiveNotificationResponse: UNNotificationResponse,
            withCompletionHandler: () -> Unit
        ) {
            val actionId = didReceiveNotificationResponse.actionIdentifier
            if (actionId == "STOP_ACTION" || actionId == UNNotificationDefaultActionIdentifier) {
                dispatch_async(dispatch_get_main_queue()) {
                    onStopReached?.invoke()
                }
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
            title = "Stop",
            options = UNNotificationActionOptionForeground
        )

        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = "ALARM_CATEGORY",
            actions = listOf(stopAction),
            intentIdentifiers = emptyList<String>(),
            options = UNNotificationCategoryOptionCustomDismissAction
        )

        center.setNotificationCategories(setOf(category))

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(description)
            setCategoryIdentifier("ALARM_CATEGORY")
            setSound(UNNotificationSound.soundNamed("alarm_sound.mp3"))
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
            }
        }
    }
}
