package org.yusufteker.routealarm.feature.location.data

import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer
import org.yusufteker.routealarm.feature.location.domain.calculateDistance
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import kotlinx.coroutines.withContext
import org.yusufteker.routealarm.core.presentation.popup.GoalReachedPopup
import org.yusufteker.routealarm.core.presentation.popup.PopupManager
import org.yusufteker.routealarm.settings.SettingsManager

private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var locationCallback: LocationCallback

class LocationTrackingService() : Service() {
    private var alarmRepository: AlarmRepository = getKoin().get<AlarmRepository>()
    private var alarmSoundPlayer: AlarmSoundPlayer = getKoin().get<AlarmSoundPlayer>()

    private var popupManager: PopupManager = getKoin().get<PopupManager>()

    private var alarmAlreadyTriggered = false

    private val notificationManager: org.yusufteker.routealarm.notification.NotificationManager = getKoin().get<org.yusufteker.routealarm.notification.NotificationManager>()

    private val settingsManager: SettingsManager = getKoin().get<SettingsManager>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra(EXTRA_ALARM_ID, -1) ?: -1

        if (intent?.action == ACTION_STOP_SERVICE) { // callback Foreground Notification Durdur
            alarmAlreadyTriggered = false
            if (alarmId != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    deactivateAlarm(alarmId)
                    cancelAllNotification()
                    popupManager.dismissAll()
                }
            }
            stopSelf()
            return START_NOT_STICKY
        } else if (intent?.action == ACTION_STOP_ACHIEVED) {
            if (alarmId != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    alarmSoundPlayer.stop()
                    alarmAlreadyTriggered = false
                    val alarm = alarmRepository.getAlarmByIdWithStops(alarmId)
                    if (alarm != null) {
                        cancelNotificationByID(STOP_ACHIEVED_NOTIFICATION_ID)
                    }
                    popupManager.dismissAll()
                }
            }
            return START_NOT_STICKY
        }
        notificationManager.createNotificationChannel()

        if (alarmId != -1) {

            CoroutineScope(Dispatchers.IO).launch {
                val alarm = alarmRepository.getAlarmByIdWithStops(alarmId)
                if (alarm != null) {
                    // 🔔 alarm ve alarm.stops burada
                    // Konum kontrolünü burada başlatabilirsin
                    startLocationUpdates(alarm)
                    startForeground(FOREGROUND_NOTIFICATION_ID, notificationManager.createForegroundNotification(alarmId, alarm.stops.size))
                }
            }

        }
        return START_STICKY
    }

    private fun startLocationUpdates(alarm: Alarm?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10_000L
        ).setMinUpdateIntervalMillis(5_000L).build()




        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->

                    CoroutineScope(Dispatchers.IO).launch {

                         val alarm = alarmRepository.getAlarmByIdWithStops(alarm!!.id)

                        Log.d("LocationTrackingService", "AlarmId: ${alarm?.id} -> Konum: ${location.latitude}, ${location.longitude}")

                        if (alarm != null) {

                            val lastStop = alarm.stops.first { it ->
                                !it.isPassed
                            }
                            val distance = calculateDistance(
                                location.latitude,
                                location.longitude,
                                lastStop.latitude,
                                lastStop.longitude
                            )
                            println("Last Stop: ${lastStop.name} -> Mesafe: ${formatDistance(distance)}")

                            if (!alarmAlreadyTriggered && distance < settingsManager.stopProximityThresholdMeters.first()) { // callback achieved
                                Log.d("LocationTrackingService", "Yeterince yaklaştı alarm çalıyor mesafe ${formatDistance(distance)}")
                                onLocationAchieved(alarm, lastStop.id)
                            }

                        }
                    }

                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private suspend fun onLocationAchieved(alarm: Alarm, lastStopId: Int) {
        alarmAlreadyTriggered = true
        Log.d("alarmAlreadyTriggered", "$alarmAlreadyTriggered")
        alarmSoundPlayer.play()

        CoroutineScope(Dispatchers.IO).launch {

            alarmRepository.setStopIsPassed(lastStopId, true)
            Log.d("LocationTrackingService", " onLocationAchieved setStopIsPassed $lastStopId")
            alarmRepository.triggerAlarmUpdate(alarm.id)
            Log.d("LocationTrackingService", " onLocationAchieved Achieved stop id: $lastStopId")

        }
        val currentStopIndex = alarm.stops.indexOfFirst { it.id == lastStopId }
        notificationManager.updateForegroundNotification(alarm.id, alarm.stops.size, currentStopIndex + 1 )

        if (alarm.stops.last().id == lastStopId) { // SON DURAK

            deactivateAlarm(alarm.id)

            popupManager.showCustom(
                content ={
                    GoalReachedPopup({ it()
                        this.startService(stopIntent(ACTION_STOP_SERVICE, alarm.id))
                        alarmAlreadyTriggered = false
                        alarmSoundPlayer.stop()
                    })
                } ,
                onDismiss = {}
            )

            notificationManager.showAlarmNotificationWithFullScreenIntent( alarm.stops.get(currentStopIndex).name, alarm.id, ACTION_STOP_SERVICE)


        } else { // SON DURAK DEĞİL

            popupManager.showCustom(
                content ={
                    GoalReachedPopup({
                        it()
                        alarmSoundPlayer.stop()
                        alarmAlreadyTriggered = false
                        cancelNotificationByID(STOP_ACHIEVED_NOTIFICATION_ID)
                    })
                } ,
                onDismiss = {}
            )
            notificationManager.showAlarmNotificationWithFullScreenIntent(alarm.stops.get(currentStopIndex).name, alarm.id, ACTION_STOP_ACHIEVED)


        }


    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1001

        private const val STOP_ACHIEVED_NOTIFICATION_ID = FOREGROUND_NOTIFICATION_ID + 1

        private const val CHANNEL_ID = "routealarm_location_channel"
        private const val EXTRA_ALARM_ID = "alarm_id"
        private const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_STOP_ACHIEVED = "ACTION_STOP_ACHIEVED"


    }



    private fun cancelNotificationByID(notificationId: Int) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    private fun cancelAllNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun deactivateAlarm(alarmId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmRepository.setAllStopIsPassed(false)
            alarmRepository.setAlarmActive(alarmId, false)
            withContext(Dispatchers.Main) {
                stopLocationUpdates()
            }

        }
    }


    override fun onDestroy() {
        Log.d("LocationTrackingService", "Service destroyed")
        stopLocationUpdates()
        cancelAllNotification()
        alarmSoundPlayer.stop()
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopIntent(intentAction: String, alarmId: Int): Intent {
        return Intent(this, LocationTrackingService::class.java).apply {
            action = intentAction
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
    }

}
