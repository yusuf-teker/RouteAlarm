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
import androidx.core.net.toUri
import org.jetbrains.compose.resources.getString
import org.yusufteker.routealarm.R
import org.yusufteker.routealarm.feature.alarm.presentation.AlarmActivity
import org.yusufteker.routealarm.feature.location.data.LocationTrackingService
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.approaching_destination
import routealarm.composeapp.generated.resources.location_tracking_active
import routealarm.composeapp.generated.resources.stop
import routealarm.composeapp.generated.resources.stops_remaining
import routealarm.composeapp.generated.resources.stops_remaining_camel_case

actual class NotificationManager(private val context: Context) {
    actual fun showNotification(title: String, description: String, onStopReached: () -> Unit) {
        // Implementation olmayacak
    }


     suspend fun createForegroundNotification(alarmId: Int?, stopSize: Int): Notification{

         val content = RemoteViews(context.packageName, R.layout.notification_foreground_content)
         val expandedContent = RemoteViews(context.packageName, R.layout.notification_foreground_content_expanded)


         val currentStopIndex = 0
         updateNotificationRoute(content, stopSize, currentStopIndex)
         updateNotificationRoute(expandedContent, stopSize, currentStopIndex)

         val stopIntent = stopIntent(ACTION_STOP_SERVICE, alarmId!!)
         val stopPendingIntent = PendingIntent.getService(
             context, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

         )
         // todo btnStop şuan textview sonra imageButton olacak
         content.setOnClickPendingIntent(R.id.btnStop, stopPendingIntent)
         expandedContent.setOnClickPendingIntent(R.id.btnStop, stopPendingIntent)



         return NotificationCompat.Builder(context, CHANNEL_ID)
             .setContentTitle(getString(Res.string.location_tracking_active))
             .setContentText(getString(Res.string.stops_remaining, stopSize))
             .setCustomContentView(content)
             .setCustomBigContentView(expandedContent)
             .setSmallIcon(R.drawable.ic_notification)
             .addAction(R.drawable.ic_close, getString(Res.string.stop), stopPendingIntent)
             .setOngoing(true).build()


     }

     suspend fun showAlarmNotificationWithFullScreenIntent(alarmId: Int, action: String) {
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = stopIntent(action, alarmId)
        val stopPendingIntent = PendingIntent.getService(
            context, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSoundUri = "android.resource://${context.packageName}/${R.raw.alarm_sound}".toUri()

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(
                        getString(Res.string.approaching_destination)).setContentText("").setSound(alarmSoundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .addAction(R.drawable.ic_close, getString(Res.string.stop), stopPendingIntent).setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .build()

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(STOP_ACHIEVED_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1001

        private const val STOP_ACHIEVED_NOTIFICATION_ID = FOREGROUND_NOTIFICATION_ID + 1

        private const val CHANNEL_ID = "routealarm_location_channel"
        private const val EXTRA_ALARM_ID = "alarm_id"
        private const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val ACTION_STOP_ACHIEVED = "ACTION_STOP_ACHIEVED"


    }


    private fun stopIntent(intentAction: String, alarmId: Int): Intent {
        return Intent(context, LocationTrackingService::class.java).apply {
            action = intentAction
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
    }
     fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Konum Takip Servisi", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500)
        }
        val manager = getSystemService(context,NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
    suspend fun updateNotificationRoute(
        remoteViews: RemoteViews,
        totalStops: Int,
        currentStopIndex: Int
    ) {
        remoteViews.setTextViewText(R.id.notificationTitle, getString(Res.string.stops_remaining_camel_case,totalStops-currentStopIndex ))

        val totalDots = totalStops + 1 // +1 because of the start point

        for (i in 0 until totalDots) {
            val dotId = getIdentifier("dot${i + 1}")
            remoteViews.setViewVisibility(dotId, View.VISIBLE)

            if (i == 0) {
                // Başlangıç noktası her zaman yeşil
                remoteViews.setImageViewResource(dotId, R.drawable.stop_dot_green)
            } else if (i <= currentStopIndex) {
                // Geçilen duraklar
                remoteViews.setImageViewResource(dotId, R.drawable.stop_dot_green)
            } else {
                // Henüz geçilmemiş duraklar
                remoteViews.setImageViewResource(dotId, R.drawable.stop_dot_red)
            }

            if (i < totalDots - 1) {
                val lineId = getIdentifier("line${i + 1}")
                remoteViews.setViewVisibility(lineId, View.VISIBLE)

                when {
                    i < currentStopIndex -> {
                        // Geçilen çizgiler - yeşil
                        remoteViews.setInt(lineId, "setBackgroundColor", "#00C853".toColorInt())
                    }
                    i == currentStopIndex -> {
                        // Şu anki geçiş çizgisi - açık yeşil
                        //remoteViews.setInt(lineId, "setBackgroundColor", "#80C853".toColorInt())
                        remoteViews.setImageViewResource(lineId, R.drawable.line_half_green)

                    }
                    else -> {
                        // Henüz geçilmemiş çizgi - gri
                        remoteViews.setInt(lineId, "setBackgroundColor", "#BDBDBD".toColorInt())
                    }
                }
            }
        }
    }


    suspend fun updateForegroundNotification(alarmId: Int, totalStops: Int, currentStopIndex: Int) {
        val content = RemoteViews(context.packageName, R.layout.notification_foreground_content)
        val expandedContent =  RemoteViews(context.packageName, R.layout.notification_foreground_content_expanded)
        updateNotificationRoute(content, totalStops, currentStopIndex)

        val stopIntent = stopIntent(ACTION_STOP_SERVICE, alarmId)
        val stopPendingIntent = PendingIntent.getService(
            context, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getString(Res.string.location_tracking_active))
            .setCustomContentView(content)
            .setCustomBigContentView(expandedContent)
            .setSmallIcon(R.drawable.ic_notification)
            .addAction(R.drawable.ic_close, getString(Res.string.stop), stopPendingIntent)
            .setOngoing(true)
            .build()

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(FOREGROUND_NOTIFICATION_ID, notification)
    }

}

private fun getIdentifier(name: String): Int {
    return R.id::class.java.getDeclaredField(name).getInt(null)
}