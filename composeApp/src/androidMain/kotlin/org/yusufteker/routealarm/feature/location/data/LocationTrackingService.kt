package org.yusufteker.routealarm.feature.location.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import org.yusufteker.routealarm.R
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.alarm.presentation.AlarmActivity
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer
import org.yusufteker.routealarm.feature.location.domain.STOP_PROXIMITY_THRESHOLD_METERS
import org.yusufteker.routealarm.feature.location.domain.calculateDistance
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import androidx.core.net.toUri

private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var locationCallback: LocationCallback
class LocationTrackingService() : Service() {
    private var alarmRepository: AlarmRepository = getKoin().get<AlarmRepository>()
    private var alarmSoundPlayer: AlarmSoundPlayer = getKoin().get<AlarmSoundPlayer>()

    private var alarmAlreadyTriggered = false


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra(EXTRA_ALARM_ID, -1) ?: -1

        if (intent?.action == ACTION_STOP_SERVICE) { // callback Foreground Notification Durdur
            if (alarmId != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    deactivateAlarm(alarmId)
                    cancelAllNotification()
                }
            }
            stopSelf()
            return START_NOT_STICKY
        }else if (intent?.action == ACTION_STOP_ACHIEVED){
            if (alarmId != -1){
                CoroutineScope(Dispatchers.IO).launch {
                    val alarm = alarmRepository.getAlarmByIdWithStops(alarmId)
                    if (alarm != null) {
                        cancelNotificationByID(STOP_ACHIEVED_NOTIFICATION_ID)
                    }
                }
            }
        }

        createNotificationChannel()

        if (alarmId != -1) {
            println("Tracking started for alarmId: $alarmId")
        }
        if (alarmId != -1) {

            CoroutineScope(Dispatchers.IO).launch {
                val alarm = alarmRepository.getAlarmByIdWithStops(alarmId)
                if (alarm != null) {
                    // 🔔 alarm ve alarm.stops burada
                    // Konum kontrolünü burada başlatabilirsin
                    startLocationUpdates(alarm)
                }
            }

            startForeground(FOREGROUND_NOTIFICATION_ID, createNotification(alarmId))
        }
        return START_STICKY
    }

    private fun startLocationUpdates(alarm: Alarm?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10_000L
        ).setMinUpdateIntervalMillis(5_000L)
            .build()



        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    println("AlarmId: ${alarm?.id} -> Konum: ${location.latitude}, ${location.longitude}")

                    if (alarm != null){
                        val lastStop = alarm.stops.first{ it ->
                            !it.isPassed
                        }
                        val distance = calculateDistance(
                            location.latitude, location.longitude,
                            lastStop.latitude, lastStop.longitude
                        )
                        println("Last Stop: ${lastStop.name} -> Mesafe: ${formatDistance(distance)}")
                        if (!alarmAlreadyTriggered && distance < STOP_PROXIMITY_THRESHOLD_METERS){ // callback achieved
                            println("Yeterince yaklaştı alarm çalıyor mesafe ${formatDistance(distance)}")
                            onLocationAchieved(alarm,lastStop.id)
                        }

                    }

                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    private fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun onLocationAchieved(alarm: Alarm, lastStopId: Int){
        alarmAlreadyTriggered = true
        alarmSoundPlayer.play()

        CoroutineScope(Dispatchers.IO).launch {

            alarmRepository.markStopAsPassed(lastStopId)
            Log.d("onLocationAchieved",  "Achieved stop id: $lastStopId")
            Log.d("onLocationAchieved",  "Last stop id: ${alarm.stops.last().id}")

        }
        if(alarm.stops.last().id == lastStopId){ // SON DURAK
            deactivateAlarm(alarm.id)
            showAlarmNotificationWithFullScreenIntent(alarm.id,ACTION_STOP_SERVICE)
        }else{ // SON DURAK DEĞİL
            showAlarmNotificationWithFullScreenIntent(alarm.id,ACTION_STOP_ACHIEVED)
        }



        //openAlarmActivity()
    }

    private fun createNotification(alarmId: Int?): Notification {
        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = ACTION_STOP_SERVICE
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Konum Takibi Aktif")
            .setContentText("Alarm ID: $alarmId izleniyor.")
            .setSmallIcon(R.drawable.ic_notification)
            .addAction(R.drawable.ic_close, "Durdur", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Konum Takip Servisi",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1001

        private const val STOP_ACHIEVED_NOTIFICATION_ID = FOREGROUND_NOTIFICATION_ID + 1

        private const val CHANNEL_ID = "routealarm_location_channel"
        private const val EXTRA_ALARM_ID = "alarm_id"
        private const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val ACTION_STOP_ACHIEVED = "ACTION_STOP_ACHIEVED"



    }

    private fun showAlarmNotificationWithFullScreenIntent(alarmId: Int, action: String) {
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            this.action = action
            putExtra("alarm_id", alarmId)
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSoundUri = "android.resource://${packageName}/${R.raw.alarm_sound}".toUri()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hedefe Yaklaştınız")
            .setContentText("")
            .setSound(alarmSoundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_close, "Durdur", stopPendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(STOP_ACHIEVED_NOTIFICATION_ID, notification)
    }


    private fun openAlarmActivity(){
        Log.d("LocationTrackingService", "Trying to open AlarmActivity")

        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
        }

        // Uygulama kapalıyken bile çalışması için PendingIntent kullan
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            pendingIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            Log.e("LocationTracking", "PendingIntent canceled", e)
        }

    }

    private fun cancelNotificationByID(notificationId: Int){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    private fun cancelAllNotification(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun deactivateAlarm(alarmId: Int){
        CoroutineScope(Dispatchers.IO).launch {
            alarmRepository.setAlarmActive(alarmId, false)
        }
    }


    override fun onDestroy() {
        Log.d("LocationTrackingService", "Service destroyed")
        stopLocationUpdates()
        cancelAllNotification()
        alarmSoundPlayer.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null


}
