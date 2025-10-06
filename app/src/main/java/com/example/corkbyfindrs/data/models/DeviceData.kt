package com.sajsoft.cork2025.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement // For potential raw parsing if needed

// Simplified DeviceType for now
enum class DeviceTypeCompat {
    DELIGHT_FRONT,
    DELIGHT_REAR,
    DELIGHT_CORK,
    UNKNOWN
}

@Serializable
data class Device(
    @SerialName("device_id") val deviceId: Int,
    val address: String,
    @SerialName("secret") val secret: String, // Assuming it's a hex string from server
    @SerialName("bike_id") val bikeId: Int? = null,
    @SerialName("alert_state") val alertState: Int? = null,
    val name: String? = null,

    // Embedded LogData fields
    @SerialName("max_id") val maxLogId: String? = null, // From "max_id"
    @SerialName("log_time") val logTime: String? = null,
    @SerialName("reporter_id") val reporterId: String? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val rssi: Int? = null,
    val accuracy: Int? = null,
    val temperature: Int? = null,
    val parked: Int? = null,
    val alert: Int? = null,
    @SerialName("batt_critical") val battCritical: Int? = null,
    @SerialName("batt_voltage") val battVoltage: Int? = null,

    @SerialName("device_type") val deviceTypeRaw: JsonElement? = null
)

@Serializable
data class UsersDevicesResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: List<String>,
    @SerialName("DEVICES") val devices: List<Device> = emptyList()
)

// Conversion logic for device_type
fun Device.getDeviceTypeCompat(): DeviceTypeCompat {
    val typeInt = try {
        when (deviceTypeRaw?.toString()) {
            "\"0\"" -> 0
            "0" -> 0
            "\"1\"" -> 1
            "1" -> 1
            "\"2\"" -> 2
            "2" -> 2
            else -> deviceTypeRaw?.toString()?.toIntOrNull()
        }
    } catch (e: Exception) {
        null
    }

    return when (typeInt) {
        0 -> DeviceTypeCompat.DELIGHT_FRONT
        1 -> DeviceTypeCompat.DELIGHT_REAR
        2 -> DeviceTypeCompat.DELIGHT_CORK
        else -> DeviceTypeCompat.UNKNOWN
    }
}

// --- New Data Classes for UsersBikes ---

@Serializable
data class BikeLogEntry(
    @SerialName("device_id") val deviceId: Int,
    @SerialName("log_time") val logTime: String? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val accuracy: Int? = null,
    val rssi: Int? = null,
    val temperature: Int? = null,
    val parked: Int? = null, // Original was Int 0/1, changed to Int?
    val alert: Int? = null,   // Original was Int 0/1, changed to Int?
    @SerialName("batt_critical") val battCritical: Int? = null, // Original was Int 0/1, changed to Int?
    @SerialName("batt_voltage") val battVoltage: Int? = null,
    val address: String? = null, // Device MAC address
    @SerialName("reporter_id") val reporterId: String? = null
)

@Serializable
data class Bike(
    val icon: Int,
    @SerialName("user_id") val userId: Int,
    val id: Int, // Bike ID
    val name: String,
    @SerialName("d2d_disable") val d2dDisabled: Int, // 0 for active, 1 for disabled
    val logs: List<BikeLogEntry> = emptyList()
)

@Serializable
data class UsersBikesResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: List<String>,
    @SerialName("BIKES") val bikes: List<Bike> = emptyList()
)
