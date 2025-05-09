package org.yusufteker.routealarm.core.data.dummy

import org.yusufteker.routealarm.feature.alarm.domain.Alarm
import org.yusufteker.routealarm.feature.alarm.domain.Stop

// FakeStops.kt
val fakeStops = listOf<Stop>(
    Stop(id = 0, name = "Start", latitude = 40.7128, longitude = -74.0060, isPassed = true, alarmId = 0),
    Stop(id = 1, name = "Destination1", latitude = 40.7831, longitude = -73.9662, alarmId = 1),
    Stop(id = 2, name = "Destination2", latitude = 40.7589, longitude = -73.9851, alarmId = 2)
)

// FakeAlarms.kt
val fakeAlarms = listOf(
    Alarm(id = 0, title = "Home Route", stops = fakeStops, isActive = true, timeInMillis = 123),
    Alarm(id = 1, title = "Work Route", stops = fakeStops, isActive = false, timeInMillis = 123),
    Alarm(id = 2, title = "School Route", stops = fakeStops, isActive = false, timeInMillis = 123)
)
