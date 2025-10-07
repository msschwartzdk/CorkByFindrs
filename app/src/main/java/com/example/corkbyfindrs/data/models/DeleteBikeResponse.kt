package com.example.corkbyfindrs.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteBikeResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String,
    @SerialName("BIKE_ID") val bikeId: Int? = null // ID of the bike that was deleted
)
