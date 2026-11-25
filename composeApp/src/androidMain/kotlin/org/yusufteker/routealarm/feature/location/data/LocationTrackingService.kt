package org.yusufteker.routealarm.feature.location.data

import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin
import org.yusufteker.routealarm.core.presentation.popup.GoalReachedPopup
import org.yusufteker.routealarm.core.presentation.popup.PopupManager
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer
import org.yusufteker.routealarm.feature.location.domain.calculateDistance
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import org.yusufteker.routealarm.settings.SettingsManager

class LocationTrackingService : Service() {

    private val alarmRepository: AlarmRepository by lazy { getKoin().get() }
    private val alarmSoundPlayer: AlarmSoundPlayer by lazy { getKoin().get() }
    private val popupManager: PopupManager by lazy { getKoin().get() }
    private val notificationManager: org.yusufteker.routealarm.notification.NotificationManager by lazy {
        getKoin().get()
    }
    private val settingsManager: SettingsManager by lazy { getKoin().get() }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val locationMutex = Mutex()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    @Volatile
    private var alarmAlreadyTriggered = false

    @Volatile
    private var activeAlarmId: Int = -1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra(EXTRA_ALARM_ID, -1) ?: -1

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                alarmSoundPlayer.stop()
                alarmAlreadyTriggered = false
                val targetAlarmId = if (alarmId != -1) alarmId else activeAlarmId
                serviceScope.launch {
                    if (targetAlarmId != -1) {
                        deactivateAlarm(targetAlarmId)
                    }
                    cancelAllNotifications()
                    popupManager.dismissAll()
                    withContext(Dispatchers.Main) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
                return START_NOT_STICKY
            }

            ACTION_STOP_ACHIEVED -> {
                alarmSoundPlayer.stop()
                alarmAlreadyTriggered = false
                cancelNotificationByID(STOP_ACHIEVED_NOTIFICATION_ID)
                popupManager.dismissAll()
                return START_STICKY
            }
        }

        notificationManager.createNotificationChannels()

        if (alarmId != -1) {
            activeAlarmId = alarmId
            alarmAlreadyTriggered = false
            serviceScope.launch {
                val alarm = alarmRepository.getAlarmByIdWithStops(alarmId)
                if (alarm != null && alarm.stops.isNotEmpty()) {
                    val notification = notificationManager.createForegroundNotification(
                        alarmId = alarmId,
                        stopSize = alarm.stops.size
                    )
                    withContext(Dispatchers.Main) {
                        startForegroundCompat(notification)
                        startLocationUpdates(alarmId)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        stopSelf()
                    }
                }
            }
        } else {
            stopSelf()
        }

        return START_STICKY
    }

    private fun startForegroundCompat(notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                FOREGROUND_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    private fun startLocationUpdates(alarmId: Int) {
        stopLocationUpdates()

        val client = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient = client

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L
        ).setMinUpdateIntervalMillis(3_000L).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                serviceScope.launch {
                    locationMutex.withLock {
                        if (alarmAlreadyTriggered) return@withLock

                        val currentAlarm = alarmRepository.getAlarmByIdWithStops(alarmId) ?: return@withLock
                        if (!currentAlarm.isActive) return@withLock

                        val sortedStops = currentAlarm.stops.sortedBy { it.orderIndex }
                        val nextStop = sortedStops.firstOrNull { !it.isPassed } ?: return@withLock

                        val distance = calculateDistance(
                            location.latitude,
                            location.longitude,
                            nextStop.latitude,
                            nextStop.longitude
                        )

                        Napier.d(
                            "Stop '${nextStop.name}' distance: ${formatDistance(distance)}",
                            tag = "LocationTrackingService"
                        )

                        val threshold = settingsManager.stopProximityThresholdMeters.first()
                        if (distance <= threshold) {
                            onLocationAchieved(currentAlarm, sortedStops, nextStop)
                        }
                    }
                }
            }
        }
        locationCallback = callback

        val hasFine = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } else {
            Napier.e("Location permission missing in LocationTrackingService", tag = "LocationTrackingService")
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        val client = fusedLocationClient
        val callback = locationCallback
        if (client != null && callback != null) {
            client.removeLocationUpdates(callback)
        }
        locationCallback = null
    }

    private suspend fun onLocationAchieved(
        alarm: Alarm,
        sortedStops: List<Stop>,
        reachedStop: Stop
    ) {
        alarmAlreadyTriggered = true

        val isVibrationEnabled = alarm.isVibration && settingsManager.isVibrationEnabled.first()
        alarmSoundPlayer.play(withVibration = isVibrationEnabled)

        alarmRepository.setStopIsPassed(reachedStop.id, true)
        alarmRepository.triggerAlarmUpdate(alarm.id)

        val currentStopIndex = sortedStops.indexOfFirst { it.id == reachedStop.id }
        val isLastStop = sortedStops.lastOrNull()?.id == reachedStop.id

        notificationManager.updateForegroundNotification(
            alarmId = alarm.id,
            totalStops = sortedStops.size,
            currentStopIndex = currentStopIndex + 1
        )

        if (isLastStop) {
            withContext(Dispatchers.Main) {
                stopLocationUpdates()
            }

            popupManager.showCustom(
                content = { dismiss ->
                    GoalReachedPopup(
                        stopName = reachedStop.name,
                        onDismiss = {
                            dismiss()
                            alarmSoundPlayer.stop()
                            alarmAlreadyTriggered = false
                            serviceScope.launch {
                                deactivateAlarm(alarm.id)
                                cancelAllNotifications()
                                withContext(Dispatchers.Main) {
                                    stopForeground(STOP_FOREGROUND_REMOVE)
                                    stopSelf()
                                }
                            }
                        }
                    )
                },
                onDismiss = {}
            )

            notificationManager.showAlarmNotificationWithFullScreenIntent(
                stopName = reachedStop.name,
                alarmId = alarm.id,
                action = ACTION_STOP_SERVICE
            )
        } else {
            popupManager.showCustom(
                content = { dismiss ->
                    GoalReachedPopup(
                        stopName = reachedStop.name,
                        onDismiss = {
                            dismiss()
                            alarmSoundPlayer.stop()
                            alarmAlreadyTriggered = false
                            cancelNotificationByID(STOP_ACHIEVED_NOTIFICATION_ID)
                        }
                    )
                },
                onDismiss = {}
            )

            notificationManager.showAlarmNotificationWithFullScreenIntent(
                stopName = reachedStop.name,
                alarmId = alarm.id,
                action = ACTION_STOP_ACHIEVED
            )
        }
    }

    private fun cancelNotificationByID(notificationId: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }

    private fun cancelAllNotifications() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    private suspend fun deactivateAlarm(alarmId: Int) {
        alarmRepository.setStopsPassedForAlarm(alarmId, false)
        alarmRepository.setAlarmActive(alarmId, false)
        withContext(Dispatchers.Main) {
            stopLocationUpdates()
        }
    }

    override fun onDestroy() {
        stopLocationUpdates()
        cancelAllNotifications()
        alarmSoundPlayer.stop()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val STOP_ACHIEVED_NOTIFICATION_ID = 1002
        private const val EXTRA_ALARM_ID = "alarm_id"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_STOP_ACHIEVED = "ACTION_STOP_ACHIEVED"
    }
}
