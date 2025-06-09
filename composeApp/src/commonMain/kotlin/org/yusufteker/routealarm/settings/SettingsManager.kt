// commonMain/kotlin/.../SettingsManager.kt
package org.yusufteker.routealarm.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val STOP_PROXIMITY_THRESHOLD_METERS = intPreferencesKey("stop_proximity_threshold_meters")
    }

    val stopProximityThresholdMeters: Flow<Int> = dataStore.data.map { preferences ->
        preferences[STOP_PROXIMITY_THRESHOLD_METERS] ?: 250
    }

    suspend fun setStopProximityThresholdMeters(meters: Int) {
        dataStore.edit { preferences ->
            preferences[STOP_PROXIMITY_THRESHOLD_METERS] = meters
        }
    }

    suspend fun clearAllSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
