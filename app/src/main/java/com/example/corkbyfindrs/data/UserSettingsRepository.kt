package com.sajsoft.cork2025.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sajsoft.cork2025.data.models.Bike
import com.sajsoft.cork2025.data.models.Device
import com.sajsoft.cork2025.data.models.RideInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first // Added this import
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

// Define DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PASSWORD = stringPreferencesKey("user_password")
        val SESSION_ID = stringPreferencesKey("session_id")
        val DEVICES_JSON = stringPreferencesKey("devices_json")
        val BIKES_JSON = stringPreferencesKey("bikes_json")
        val RIDES_JSON = stringPreferencesKey("rides_json") // Key for storing rides
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val devicesList = preferences[PreferencesKeys.DEVICES_JSON]?.let { jsonString ->
                Json.decodeFromString<List<Device>>(jsonString)
            } ?: emptyList()
            val bikesList = preferences[PreferencesKeys.BIKES_JSON]?.let { jsonString ->
                Json.decodeFromString<List<Bike>>(jsonString)
            } ?: emptyList()
            val ridesMap = preferences[PreferencesKeys.RIDES_JSON]?.let { jsonString ->
                Json.decodeFromString<Map<Int, List<RideInfo>>>(jsonString)
            } ?: emptyMap()
            UserPreferences(
                email = preferences[PreferencesKeys.USER_EMAIL] ?: "",
                password = preferences[PreferencesKeys.USER_PASSWORD] ?: "",
                sessionId = preferences[PreferencesKeys.SESSION_ID] ?: "",
                devices = devicesList,
                bikes = bikesList,
                rides = ridesMap
            )
        }

    suspend fun updateEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_EMAIL] = email
        }
    }

    suspend fun updatePassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_PASSWORD] = password
        }
    }

    suspend fun updateSessionId(sessionId: String?) {
        context.dataStore.edit { preferences ->
            if (sessionId == null) {
                preferences.remove(PreferencesKeys.SESSION_ID)
                preferences.remove(PreferencesKeys.USER_EMAIL)
                preferences.remove(PreferencesKeys.USER_PASSWORD)
                // Also clear device and bike data on logout
                preferences.remove(PreferencesKeys.DEVICES_JSON)
                preferences.remove(PreferencesKeys.BIKES_JSON)
                preferences.remove(PreferencesKeys.RIDES_JSON) // Clear rides on logout
            } else {
                preferences[PreferencesKeys.SESSION_ID] = sessionId
            }
        }
    }

    suspend fun updateDevices(devices: List<Device>) {
        val jsonString = Json.encodeToString(devices)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICES_JSON] = jsonString
        }
    }

    suspend fun updateBikes(bikes: List<Bike>) {
        val jsonString = Json.encodeToString(bikes)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIKES_JSON] = jsonString
        }
    }

    // Function to update rides for a specific bike
    suspend fun updateRidesForBike(bikeId: Int, rides: List<RideInfo>) {
        context.dataStore.edit { preferences ->
            val currentRidesJson = preferences[PreferencesKeys.RIDES_JSON]
            val currentRidesMap: MutableMap<Int, List<RideInfo>> = if (currentRidesJson != null) {
                Json.decodeFromString<Map<Int, List<RideInfo>>>(currentRidesJson).toMutableMap()
            } else {
                mutableMapOf()
            }
            currentRidesMap[bikeId] = rides
            preferences[PreferencesKeys.RIDES_JSON] = Json.encodeToString(currentRidesMap)
        }
    }

    // Optional: Function to clear all rides
    suspend fun clearAllRides() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.RIDES_JSON)
        }
    }

    suspend fun getDeviceAddresses(): List<String> {
        var addresses = emptyList<String>()
        userPreferencesFlow.first().devices.let { devices ->
            addresses = devices.map { it.address }
        }
        return addresses
    }
}

data class UserPreferences(
    val email: String,
    val password: String,
    val sessionId: String,
    val devices: List<Device> = emptyList(),
    val bikes: List<Bike> = emptyList(),
    val rides: Map<Int, List<RideInfo>> = emptyMap() // Added rides to UserPreferences
)
