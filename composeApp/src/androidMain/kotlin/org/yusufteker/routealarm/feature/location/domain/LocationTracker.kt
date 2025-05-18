package org.yusufteker.routealarm.feature.location.domain

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.yusufteker.routealarm.feature.alarm.domain.AlarmRepository
import org.yusufteker.routealarm.feature.location.data.LocationTrackingService

private const val EXTRA_ALARM_ID = "alarm_id"

actual class LocationTracker(
    private val context: Context,
    private val alarmRepository: AlarmRepository

) {
    actual fun startTracking(alarmId: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            val alarmWithStops = alarmRepository.getAlarmByIdWithStops(alarmId)
            if (alarmWithStops != null) {
                val stopsJson = Json.encodeToString(alarmWithStops.stops)

                val intent = Intent(context, LocationTrackingService::class.java).apply {
                    putExtra(EXTRA_ALARM_ID, alarmWithStops.id)
                    putExtra("stops_json", stopsJson)
                }

                context.startForegroundService(intent)
            }
        }
    }

    actual fun stopTracking() {
        val intent = Intent(context, LocationTrackingService::class.java)
        context.stopService(intent)
    }
}
