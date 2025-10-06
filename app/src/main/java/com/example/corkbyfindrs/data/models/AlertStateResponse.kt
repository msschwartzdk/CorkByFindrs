package com.sajsoft.cork2025.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertStateResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String
)
