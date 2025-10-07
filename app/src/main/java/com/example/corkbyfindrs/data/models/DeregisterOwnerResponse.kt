package com.example.corkbyfindrs.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeregisterOwnerResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String
)
