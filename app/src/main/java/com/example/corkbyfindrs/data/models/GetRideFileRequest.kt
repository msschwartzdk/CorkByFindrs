package com.sajsoft.cork2025.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GetRideFileRequest(
    val session_id: String,
    val user_email: String,
    val ride_id: String
)
