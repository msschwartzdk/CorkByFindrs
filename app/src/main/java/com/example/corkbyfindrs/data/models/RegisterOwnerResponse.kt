package com.sajsoft.cork2025.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterOwnerResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String,
    @SerialName("SECRET") val secret: String? = null // Hex string, nullable
)
