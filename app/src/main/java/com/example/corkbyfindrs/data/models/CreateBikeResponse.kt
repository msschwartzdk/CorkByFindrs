package com.example.corkbyfindrs.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBikeResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String,
    @SerialName("BIKE_ID") val bikeId: Int? = null,
    @SerialName("BIKE_NAME") val bikeName: String? = null,
    @SerialName("ICON_ID") val iconId: Int? = null
)
