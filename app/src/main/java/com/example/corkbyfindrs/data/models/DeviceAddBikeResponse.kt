package com.example.corkbyfindrs.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceAddBikeResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String
    // Potentially other fields if the server sends more, but Java client didn't use them.
)
