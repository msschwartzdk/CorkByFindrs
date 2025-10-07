package com.example.corkbyfindrs.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Assuming BikeLogEntry is already defined in DeviceData.kt (or a similar shared location)
// from the UsersBikesResponse implementation. If not, it needs to be defined or imported.
// For this subtask, we'll assume BikeLogEntry is accessible.

@Serializable
data class GetLogsResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: List<String>, // Changed from String to List<String>
    @SerialName("LOGS") val logs: List<BikeLogEntry> = emptyList()
)
