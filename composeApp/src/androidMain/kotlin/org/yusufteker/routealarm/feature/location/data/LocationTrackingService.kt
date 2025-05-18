package org.yusufteker.routealarm.feature.location.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.IBinder
import android.os.Looper
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
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.getKoin
import org.koin.java.KoinJavaComponent.inject
import org.yusufteker.routealarm.R
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.location.domain.STOP_PROXIMITY_THRESHOLD_METERS
import org.yusufteker.routealarm.feature.location.domain.calculateDistance
import routealarm.composeapp.generated.resources.Res
import javax.inject.Inject

private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var locationCallback: LocationCallback
class LocationTrackingService() : Service() {
    private var alarmRepository: AlarmRepository = getKoin().get<AlarmRepository>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra(EXTRA_ALARM_ID, -1) ?: -1

        if (intent?.action == ACTION_STOP_SERVICE) {
            if (alarmId != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    alarmRepository.setAlarmActive(alarmId, false)
                }
            }
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()

        if (alarmId != null && alarmId != -1) {
            println("Tracking started for alarmId: $alarmId")
        }
        if (alarmId != null && alarmId != -1) {

            CoroutineScope(Dispatchers.IO).launch {
                val alarm = alarmRepository.getAlarmByIdWithStops(alarmId)
                if (alarm != null) {
                    // 🔔 alarm ve alarm.stops burada
                    // Konum kontrolünü burada başlatabilirsin
                    startLocationUpdates(alarm)
                }
            }

            startForeground(NOTIFICATION_ID, createNotification(alarmId))
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLocationUpdates(alarm: Alarm?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10_000L
        )
            .setMinUpdateIntervalMillis(5_000L)
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
                        println("Last Stop: ${lastStop.name} -> Mesafe: $distance m")
                        if (distance < STOP_PROXIMITY_THRESHOLD_METERS){
                            CoroutineScope(Dispatchers.IO).launch {
                                alarmRepository.setAlarmActive(alarm.id, false)
                            }
                            playAlarmSound()

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
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun playAlarmSound() {
        // TODO ALARM ÇAL
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "routealarm_location_channel"
        private const val EXTRA_ALARM_ID = "alarm_id"
        private const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    }
}
