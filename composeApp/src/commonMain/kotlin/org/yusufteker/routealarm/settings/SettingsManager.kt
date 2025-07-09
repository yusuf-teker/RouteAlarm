// commonMain/kotlin/.../SettingsManager.kt
package org.yusufteker.routealarm.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val STOP_PROXIMITY_THRESHOLD_METERS = intPreferencesKey("stop_proximity_threshold_meters")
        private val START_LATITUDE = doublePreferencesKey("start_location_latitude")

        private val START_NAME = stringPreferencesKey("start_location_name")
            private val START_LONGITUDE = doublePreferencesKey("start_location_longitude")

        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed") // 0 = false, 1 = true

    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun isOnboardingCompleted(): Boolean =
        dataStore.data.first()[ONBOARDING_COMPLETED] ?: false

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }


    val stopProximityThresholdMeters: Flow<Int> = dataStore.data.map { preferences ->
        preferences[STOP_PROXIMITY_THRESHOLD_METERS] ?: 250
    }

    val startLocation: Flow<Triple<String, Double, Double>?> = dataStore.data.map { preferences ->
        val name = preferences[START_NAME]
        val lat = preferences[START_LATITUDE]
        val lon = preferences[START_LONGITUDE]
        if (lat != null && lon != null && name != null) Triple(name, lat, lon) else null
    }
    suspend fun setStopProximityThresholdMeters(meters: Int) {
        dataStore.edit { preferences ->
            preferences[STOP_PROXIMITY_THRESHOLD_METERS] = meters
        }
    }

    suspend fun setStartLocation(name: String?, latitude: Double?, longitude: Double?) {
        dataStore.edit { preferences ->
            preferences[START_LATITUDE] = latitude ?: 0.0
            preferences[START_LONGITUDE] = longitude ?: 0.0
            preferences[START_NAME] = name ?: ""
        }
    }

    suspend fun clearAllSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
