package org.yusufteker.routealarm.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.toColorInt
import org.jetbrains.compose.resources.getString
import org.yusufteker.routealarm.MainActivity
import org.yusufteker.routealarm.R
import org.yusufteker.routealarm.feature.alarm.presentation.AlarmActivity
import org.yusufteker.routealarm.feature.location.data.LocationTrackingService
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.approaching_destination
import routealarm.composeapp.generated.resources.location_tracking_active
import routealarm.composeapp.generated.resources.stop
import routealarm.composeapp.generated.resources.stop_reached_message
import routealarm.composeapp.generated.resources.stops_remaining
import routealarm.composeapp.generated.resources.stops_remaining_camel_case

actual class NotificationManager(private val context: Context) {

    actual fun showNotification(title: String, description: String, onStopReached: () -> Unit) {
        // Android uses createForegroundNotification & showAlarmNotificationWithFullScreenIntent
    }

    suspend fun createForegroundNotification(alarmId: Int, stopSize: Int): Notification {
        createNotificationChannels()

        val content = createNotificationViews(R.layout.notification_foreground_content, stopSize, 0)
        val expandedContent = createNotificationViews(R.layout.notification_foreground_content_expanded, stopSize, 0)

        val stopPendingIntent = createStopPendingIntent(alarmId, ACTION_STOP_SERVICE, requestCode = 101)
        val openAppIntent = createOpenAppPendingIntent()

        content.setOnClickPendingIntent(R.id.btnStop, stopPendingIntent)
        expandedContent.setOnClickPendingIntent(R.id.btnStop, stopPendingIntent)

        return NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
            .setContentTitle(getString(Res.string.location_tracking_active))
            .setContentText(getString(Res.string.stops_remaining, stopSize))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(content)
            .setCustomBigContentView(expandedContent)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppIntent)
            .addAction(R.drawable.ic_close, getString(Res.string.stop), stopPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    suspend fun showAlarmNotificationWithFullScreenIntent(
        stopName: String,
        alarmId: Int,
        action: String
    ) {
        createNotificationChannels()

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmActivity.EXTRA_ACTION, action)
            putExtra(AlarmActivity.EXTRA_STOP_NAME, stopName)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            201,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = createStopPendingIntent(alarmId, action, requestCode = 202)

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(Res.string.approaching_destination))
            .setContentText(getString(Res.string.stop_reached_message, stopName))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(R.drawable.ic_close, getString(Res.string.stop), stopPendingIntent)
            .setAutoCancel(true)
            .setSilent(true) // Audio & vibration handled directly by AlarmSoundPlayer on USAGE_ALARM
            .build()

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(STOP_ACHIEVED_NOTIFICATION_ID, notification)
    }

    fun createNotificationChannels() {
        val manager = getSystemService(context, NotificationManager::class.java) ?: return

        val trackingChannel = NotificationChannel(
            TRACKING_CHANNEL_ID,
            "Route Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active route tracking progress"
            setShowBadge(false)
        }

        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Stop Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when approaching your stop"
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannel(trackingChannel)
        manager.createNotificationChannel(alarmChannel)
    }

    suspend fun updateNotificationRoute(
        remoteViews: RemoteViews,
        totalStops: Int,
        currentStopIndex: Int
    ) {
        val remaining = (totalStops - currentStopIndex).coerceAtLeast(0)
        remoteViews.setTextViewText(
            R.id.notificationTitle,
            getString(Res.string.stops_remaining_camel_case, remaining)
        )

        val dotIds = intArrayOf(R.id.dot1, R.id.dot2, R.id.dot3, R.id.dot4, R.id.dot5, R.id.dot6)
        val lineIds = intArrayOf(R.id.line1, R.id.line2, R.id.line3, R.id.line4, R.id.line5)

        val totalDots = (totalStops + 1).coerceIn(2, dotIds.size)
        val scaledPassedIndex = if (totalStops + 1 > dotIds.size && totalStops > 0) {
            ((currentStopIndex.toFloat() / totalStops.toFloat()) * (dotIds.size - 1)).toInt()
        } else {
            currentStopIndex
        }

        val greenColor = "#10B981".toColorInt()
        val redColor = "#EF4444".toColorInt()
        val grayColor = "#64748B".toColorInt()

        for (i in dotIds.indices) {
            val dotId = dotIds[i]
            if (i < totalDots) {
                remoteViews.setViewVisibility(dotId, View.VISIBLE)
                remoteViews.setImageViewResource(dotId, R.drawable.map_pin)
                val color = if (i == 0 || i <= scaledPassedIndex) greenColor else redColor
                remoteViews.setInt(dotId, "setColorFilter", color)
            } else {
                remoteViews.setViewVisibility(dotId, View.GONE)
            }

            if (i < lineIds.size) {
                val lineId = lineIds[i]
                if (i < totalDots - 1) {
                    remoteViews.setViewVisibility(lineId, View.VISIBLE)
                    when {
                        i < scaledPassedIndex -> {
                            remoteViews.setImageViewResource(lineId, 0)
                            remoteViews.setInt(lineId, "setBackgroundColor", greenColor)
                        }
                        i == scaledPassedIndex -> {
                            remoteViews.setInt(lineId, "setBackgroundColor", 0)
                            remoteViews.setImageViewResource(lineId, R.drawable.line_half_green)
                        }
                        else -> {
                            remoteViews.setImageViewResource(lineId, 0)
                            remoteViews.setInt(lineId, "setBackgroundColor", grayColor)
                        }
                    }
                } else {
                    remoteViews.setViewVisibility(lineId, View.GONE)
                }
            }
        }
    }

    suspend fun updateForegroundNotification(alarmId: Int, totalStops: Int, currentStopIndex: Int) {
        val content = RemoteViews(context.packageName, R.layout.notification_foreground_content)
        val expandedContent = RemoteViews(context.packageName, R.layout.notification_foreground_content_expanded)
        updateNotificationRoute(content, totalStops, currentStopIndex)
        updateNotificationRoute(expandedContent, totalStops, currentStopIndex)

        val stopPendingIntent = createStopPendingIntent(alarmId, ACTION_STOP_SERVICE, requestCode = 101)
        val openAppIntent = createOpenAppPendingIntent()

        content.setOnClickPendingIntent(R.id.btnStop, stopPendingIntent)
        expandedContent.setOnClickPendingIntent(R.id.btnStop, stopPendingIntent)

        val remaining = (totalStops - currentStopIndex).coerceAtLeast(0)
        val notification = NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
            .setContentTitle(getString(Res.string.location_tracking_active))
            .setContentText(getString(Res.string.stops_remaining, remaining))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(content)
            .setCustomBigContentView(expandedContent)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppIntent)
            .addAction(R.drawable.ic_close, getString(Res.string.stop), stopPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private suspend fun createNotificationViews(
        layoutId: Int,
        totalStops: Int,
        currentStopIndex: Int
    ): RemoteViews {
        val views = RemoteViews(context.packageName, layoutId)
        updateNotificationRoute(views, totalStops, currentStopIndex)
        return views
    }

    private fun createStopPendingIntent(
        alarmId: Int,
        action: String = ACTION_STOP_SERVICE,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            this.action = action
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createOpenAppPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 1001
        const val STOP_ACHIEVED_NOTIFICATION_ID = 1002

        private const val TRACKING_CHANNEL_ID = "routealarm_tracking_channel"
        private const val ALARM_CHANNEL_ID = "routealarm_alarm_channel"
        private const val EXTRA_ALARM_ID = "alarm_id"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_STOP_ACHIEVED = "ACTION_STOP_ACHIEVED"
    }
}
