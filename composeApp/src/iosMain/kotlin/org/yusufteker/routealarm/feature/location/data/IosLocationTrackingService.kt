package org.yusufteker.routealarm.feature.location.data

import org.yusufteker.routealarm.feature.location.domain.Location
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.*
import platform.darwin.NSObject
import platform.Foundation.NSError
import kotlinx.cinterop.useContents
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.mp.KoinPlatform.getKoin
import org.yusufteker.routealarm.core.presentation.popup.GoalReachedPopup
import org.yusufteker.routealarm.core.presentation.popup.PopupManager
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.alarm.domain.Stop
import org.yusufteker.routealarm.feature.location.domain.AlarmSoundPlayer
import org.yusufteker.routealarm.feature.location.domain.calculateDistance
import org.yusufteker.routealarm.feature.location.domain.formatDistance
import org.yusufteker.routealarm.notification.NotificationManager
import org.yusufteker.routealarm.settings.SettingsManager

class IosLocationTrackingService {
    private val locationManager = CLLocationManager()
    private var onStopReached: ((Stop, Boolean) -> Unit)? = null
    private var delegate: CLLocationManagerDelegateProtocol? = null

    private var alarmRepository: AlarmRepository = getKoin().get<AlarmRepository>()
    private var alarmSoundPlayer: AlarmSoundPlayer = getKoin().get<AlarmSoundPlayer>()
    private var popupManager: PopupManager = getKoin().get<PopupManager>()
    private var settingsManager: SettingsManager = getKoin().get<SettingsManager>()

    private val alreadyTriggeredStops = mutableSetOf<Int>()
    private val not = NotificationManager()
    private var activeAlarmId: Int? = null

    // Add these to prevent race conditions
    private val locationUpdateMutex = Mutex()
    private var isProcessingLocation = false
    private var cachedThreshold: Double? = null

    @OptIn(ExperimentalForeignApi::class)
    fun startLocationUpdates(
        alarmId: Int,
        onStopReached: (Stop, Boolean) -> Unit
    ) {
        this.onStopReached = onStopReached
        this.activeAlarmId = alarmId

        // Cache the threshold value once at start
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cachedThreshold = settingsManager.stopProximityThresholdMeters.first().toDouble()
            } catch (e: Exception) {
                println("Error getting threshold: ${e.message}")
                cachedThreshold = 100.0 // Default fallback
            }
        }

        val newDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val loc = (didUpdateLocations.firstOrNull() as? CLLocation)
                if (loc != null) {
                    val (lat, lng) = loc.coordinate.useContents {
                        latitude to longitude
                    }
                    val currentLocation = Location(
                        name = "",
                        lat = lat,
                        lng = lng
                    )

                    handleLocationUpdate(currentLocation)
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                when (didFailWithError.code.toInt()) {
                    0 -> println("Konum bilinmiyor, geçici bir durum.")
                    1 -> println("Kullanıcı konum izni vermedi.")
                    2 -> println("Ağ hatası nedeniyle konum alınamadı.")
                    else -> println("Konum hatası: ${didFailWithError.code} - ${didFailWithError.localizedDescription}")
                }
            }
        }

        delegate = newDelegate

        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
        locationManager.delegate = newDelegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = 10.0
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
    }

    fun stopLocationUpdates() {
        locationManager.stopUpdatingLocation()
        cachedThreshold = null
        activeAlarmId = null
        alreadyTriggeredStops.clear()
    }

    private fun handleLocationUpdate(currentLocation: Location) {
        val alarmId = activeAlarmId ?: return
        val threshold = cachedThreshold ?: return

        // Prevent multiple simultaneous processing
        if (isProcessingLocation) return

        CoroutineScope(Dispatchers.IO).launch {
            locationUpdateMutex.withLock {
                try {
                    isProcessingLocation = true
                    processLocationUpdate(currentLocation, alarmId, threshold)
                } catch (e: Exception) {
                    println("Error processing location update: ${e.message}")
                } finally {
                    isProcessingLocation = false
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

        // Find the next stop that hasn't been passed and hasn't been triggered
        val nextStop = alarm.stops.firstOrNull { stop ->
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
            handleStopReached(nextStop, alarm, alarmId)
        }
    }

    private suspend fun handleStopReached(stop: Stop, alarm: org.yusufteker.routealarm.feature.alarm.domain.Alarm, alarmId: Int) {
        // Mark as triggered immediately to prevent double processing
        alreadyTriggeredStops.add(stop.id)

        // Play alarm and show notifications on Main thread
        withContext(Dispatchers.Main) {
            alarmSoundPlayer.play()
            showLocationReachedNotification()
            showLocationReachedPopup()
        }

        // Update database
        alarmRepository.setStopIsPassed(stop.id, true)
        alarmRepository.triggerAlarmUpdate(alarm.id)

        val isLastStop = alarm.stops.last().id == stop.id

        // Invoke callback on Main thread
        withContext(Dispatchers.Main) {
            onStopReached?.invoke(stop, isLastStop)
        }

        if (isLastStop) {
            alarmRepository.setAlarmActive(alarmId, false)
            alarmRepository.setAllStopIsPassed(false)
            activeAlarmId = null
        }
    }

    private fun showLocationReachedPopup() {
        popupManager.showCustom(
            content = {
                GoalReachedPopup {
                    it()
                    alarmSoundPlayer.stop()
                    alreadyTriggeredStops.clear()
                }
            },
            onDismiss = {
                // Handle dismiss if needed
            }
        )
    }

    private fun showLocationReachedNotification() {
        not.showNotification(
            "Hedefe ulaşıldı.",
            "Konuma Yaklaştın 3",
            onStopReached = {
                println("showLocationReachedNotification on stop reached tıklandı")
                alarmSoundPlayer.stop()
                alreadyTriggeredStops.clear()
                popupManager.dismissAll()
            }
        )
    }
}