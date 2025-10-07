package com.example.corkbyfindrs.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: List<String>,
    @SerialName("SESSION_ID") val sessionId: String? = null
)
