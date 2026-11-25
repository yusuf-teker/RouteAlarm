package org.yusufteker.routealarm.feature.location.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import org.yusufteker.routealarm.core.presentation.popup.GoalReachedPopup
import org.yusufteker.routealarm.core.presentation.popup.PopupManager
import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.calculateDistance
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import org.yusufteker.routealarm.notification.NotificationManager
import org.yusufteker.routealarm.settings.SettingsManager
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.approaching_destination
import routealarm.composeapp.generated.resources.stop_reached_message

class IosLocationTrackingService {
    private val locationManager = CLLocationManager()
    private var onStopReached: ((Stop, Boolean) -> Unit)? = null
    private var delegate: CLLocationManagerDelegateProtocol? = null

    private val alarmRepository: AlarmRepository by lazy { getKoin().get() }
    private val alarmSoundPlayer: AlarmSoundPlayer by lazy { getKoin().get() }
    private val popupManager: PopupManager by lazy { getKoin().get() }
    private val settingsManager: SettingsManager by lazy { getKoin().get() }
    private val notificationManager: NotificationManager by lazy { getKoin().get() }

    private val trackingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val locationUpdateMutex = Mutex()
    private val alreadyTriggeredStops = mutableSetOf<Int>()

    private var activeAlarmId: Int? = null
    private var isPausedForAlert = false

    @OptIn(ExperimentalForeignApi::class)
    fun startLocationUpdates(
        alarmId: Int,
        onStopReached: (Stop, Boolean) -> Unit
    ) {
        stopLocationUpdates()

        this.onStopReached = onStopReached
        this.activeAlarmId = alarmId
        this.isPausedForAlert = false

        val newDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val loc = didUpdateLocations.lastOrNull() as? CLLocation ?: return
                val (lat, lng) = loc.coordinate.useContents { latitude to longitude }
                handleLocationUpdate(Location(name = "", lat = lat, lng = lng))
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                println("iOS Location error: ${didFailWithError.code} - ${didFailWithError.localizedDescription}")
            }
        }

        delegate = newDelegate
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.showsBackgroundLocationIndicator = true
        locationManager.pausesLocationUpdatesAutomatically = false
        locationManager.delegate = newDelegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = 10.0
        locationManager.startUpdatingLocation()
    }

    fun stopLocationUpdates() {
        locationManager.stopUpdatingLocation()
        locationManager.delegate = null
        delegate = null
        activeAlarmId = null
        isPausedForAlert = false
        alreadyTriggeredStops.clear()
    }

    private fun handleLocationUpdate(currentLocation: Location) {
        val alarmId = activeAlarmId ?: return

        trackingScope.launch {
            locationUpdateMutex.withLock {
                if (isPausedForAlert) return@withLock
                runCatching {
                    val threshold = settingsManager.stopProximityThresholdMeters.first().toDouble()
                    processLocationUpdate(currentLocation, alarmId, threshold)
                }.onFailure { e ->
                    println("Error processing iOS location update: ${e.message}")
                }
            }
        }
    }

    private suspend fun processLocationUpdate(
        currentLocation: Location,
        alarmId: Int,
        threshold: Double
    ) {
        val alarm = alarmRepository.getAlarmByIdWithStops(alarmId) ?: return
        if (!alarm.isActive) return

        val sortedStops = alarm.stops.sortedBy { it.orderIndex }
        val nextStop = sortedStops.firstOrNull { stop ->
            !stop.isPassed && !alreadyTriggeredStops.contains(stop.id)
        } ?: return

        val distance = calculateDistance(
            currentLocation.lat,
            currentLocation.lng,
            nextStop.latitude,
            nextStop.longitude
        )

        println("Next Stop: ${nextStop.name} -> Distance: ${formatDistance(distance)}")

        if (distance <= threshold) {
            handleStopReached(nextStop, alarm, sortedStops, alarmId)
        }
    }

    private suspend fun handleStopReached(
        stop: Stop,
        alarm: Alarm,
        sortedStops: List<Stop>,
        alarmId: Int
    ) {
        alreadyTriggeredStops.add(stop.id)
        isPausedForAlert = true

        val isLastStop = sortedStops.lastOrNull()?.id == stop.id
        val isVibration = alarm.isVibration && settingsManager.isVibrationEnabled.first()

        alarmRepository.setStopIsPassed(stop.id, true)
        alarmRepository.triggerAlarmUpdate(alarm.id)

        val title = getString(Res.string.approaching_destination)
        val body = getString(Res.string.stop_reached_message, stop.name)

        withContext(Dispatchers.Main) {
            alarmSoundPlayer.play(withVibration = isVibration)

            notificationManager.showNotification(
                title = title,
                description = body,
                onStopReached = {
                    onAlertDismissed(alarmId, isLastStop)
                }
            )

            popupManager.showCustom(
                content = { dismiss ->
                    GoalReachedPopup(
                        stopName = stop.name,
                        onDismiss = {
                            dismiss()
                            onAlertDismissed(alarmId, isLastStop)
                        }
                    )
                },
                onDismiss = {}
            )

            onStopReached?.invoke(stop, isLastStop)
        }

        if (isLastStop) {
            withContext<Unit>(Dispatchers.Main) {
                locationManager.stopUpdatingLocation()
            }
        }
    }

    private fun onAlertDismissed(alarmId: Int, isLastStop: Boolean) {
        alarmSoundPlayer.stop()
        popupManager.dismissAll()
        isPausedForAlert = false

        if (isLastStop) {
            trackingScope.launch {
                alarmRepository.setAlarmActive(alarmId, false)
                alarmRepository.setStopsPassedForAlarm(alarmId, false)
            }
            stopLocationUpdates()
        }
    }
}
