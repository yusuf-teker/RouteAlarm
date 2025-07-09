package org.yusufteker.routealarm.feature.location.data

import org.yusufteker.routealarm.feature.location.domain.Location


import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.*
import platform.darwin.NSObject
import platform.Foundation.NSError
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    private var delegate: CLLocationManagerDelegateProtocol? = null // <- EKLENDİ

    private var alarmRepository: AlarmRepository = getKoin().get<AlarmRepository>()

    private var alarmSoundPlayer: AlarmSoundPlayer = getKoin().get<AlarmSoundPlayer>()

    private var popupManager: PopupManager = getKoin().get<PopupManager>()

    private var settingsManager: SettingsManager = getKoin().get<SettingsManager>()

    private val alreadyTriggeredStops = mutableSetOf<Int>()

    val  not = NotificationManager()


    private var activeAlarmId: Int? = null

    @OptIn(ExperimentalForeignApi::class)
    fun startLocationUpdates(
        alarmId: Int,
        onStopReached: (Stop,Boolean) -> Unit

    ) {
        this.onStopReached = onStopReached
        this.activeAlarmId = alarmId

        val newDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val loc = (didUpdateLocations.firstOrNull() as? CLLocation)
                if (loc != null){
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
    }

    private fun handleLocationUpdate(currentLocation: Location) {

        val alarmId = activeAlarmId ?: return

        CoroutineScope(Dispatchers.IO).launch {

            val alarm = alarmRepository.getAlarmByIdWithStops(alarmId)
            alarm?.let {
                val lastStop = alarm.stops.first { it ->
                    !it.isPassed
                }
                val distance = calculateDistance(
                    currentLocation.lat,
                    currentLocation.lng,
                    lastStop.latitude,
                    lastStop.longitude
                )
                println("Last Stop: ${lastStop.name} -> Mesafe: ${formatDistance(distance)}")

                if (distance <= settingsManager.stopProximityThresholdMeters.first()) {


                    alreadyTriggeredStops.add(lastStop.id)
                    alarmSoundPlayer.play()

                    showLocationReachedNotification()
                    showLocationReachedPopup()

                    val isLastStop =  alarm.stops.last().id == lastStop.id
                    alarmRepository.setStopIsPassed(lastStop.id, true)
                    alarmRepository.triggerAlarmUpdate(alarm.id)

                    onStopReached?.invoke(lastStop, isLastStop)

                    if (isLastStop){
                        alarmRepository.setAlarmActive(alarmId, false)
                        alarmRepository.setAllStopIsPassed(false)
                    }


                }
            }

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

            }
        )
    }
    private fun showLocationReachedNotification(){
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
