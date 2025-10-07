package com.example.corkbyfindrs.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResendActivationResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String
)
