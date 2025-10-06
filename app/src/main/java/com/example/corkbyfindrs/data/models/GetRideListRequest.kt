package com.sajsoft.cork2025.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GetRideListRequest(
    val session_id: String,
    val user_email: String,
    val bike_id: Int,
    val index: Int,
    val maxresults: Int
)
