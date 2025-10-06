package com.sajsoft.cork2025.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull

@Serializable
data class RideTotal(
    val total: String? = null // Or Int if it's always a number
)

@Serializable
data class RideInfo(
    val rideid: String? = null,
    val rideType: String? = null,
    val numberOfStops: String? = null,
    val stopDuration: String? = null,
    val distance: String? = null,
    val speed: String? = null,
    val changeTime: String? = null,
    val startPositionType: String? = null,
    val startPositionLatitude: String? = null,
    val startPositionLongitude: String? = null,
    val startPositionAltitude: String? = null,
    val startPositionSpeed: String? = null,
    val startPositionAccuracy: String? = null,
    val startPositionSpeedAccuracy: String? = null,
    val startPositionTime: String? = null,
    val endPositionType: String? = null,
    val endPositionLatitude: String? = null,
    val endPositionLongitude: String? = null,
    val endPositionAltitude: String? = null,
    val endPositionSpeed: String? = null,
    val endPositionAccuracy: String? = null,
    val endPositionSpeedAccuracy: String? = null,
    val endPositionTime: String? = null,
    val bike_id: String? = null,
    val name: String? = null,
    val icon: String? = null,
    val deleted: String? = null,
    val ride_ahku: String? = null,
    val version: String? = null,
    val number_of_stops: String? = null, // Note: This field name is redundant with numberOfStops. Consider consolidating if possible.
    val start_pos_time: String? = null,
    val start_pos_latitude: String? = null,
    val start_pos_longitude: String? = null,
    val start_pos_altitude: String? = null,
    val start_pos_accuracy: String? = null,
    val end_pos_time: String? = null,
    val end_pos_latitude: String? = null,
    val end_pos_longitude: String? = null,
    val end_pos_altitude: String? = null,
    val end_pos_accuracy: String? = null,
    var localFilePath: String? = null, // Path to downloaded file - Persisted
    @kotlinx.serialization.Transient var isDownloading: Boolean = false // To track download status for UI - Not persisted
)

@Serializable
data class GetRideListResponse(
    @SerialName("RV")
    val rv: Int,
    @SerialName("RIDE")
    val rideData: List<JsonElement>? = null,
    @kotlinx.serialization.Transient
    val rides: List<RideInfo>? = null,
    @kotlinx.serialization.Transient
    val totalresults: Int? = null,
    val msg: List<String>? = null // Ensuring this is List<String>?
) {
    companion object {
        fun postProcess(response: GetRideListResponse, jsonParser: kotlinx.serialization.json.Json): GetRideListResponse {
            var total: Int? = null
            val rideInfos = mutableListOf<RideInfo>()

            response.rideData?.forEachIndexed { index, element ->
                if (index == 0 && element is JsonObject && element["total"] != null) {
                    total = element["total"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                } else if (element is JsonObject) {
                    try {
                        val rideInfo = jsonParser.decodeFromJsonElement(RideInfo.serializer(), element)
                        rideInfos.add(rideInfo)
                    } catch (e: Exception) {
                        // Consider more robust error logging or handling
                        println("Error parsing RideInfo element: $element, error: ${e.message}")
                    }
                }
            }
            // Ensure all fields, including msg, are correctly passed to the new instance
            return response.copy(rides = rideInfos, totalresults = total, rideData = null, msg = response.msg)
        }
    }
}
