package com.example.corkbyfindrs.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BikeEditD2DResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String
)
