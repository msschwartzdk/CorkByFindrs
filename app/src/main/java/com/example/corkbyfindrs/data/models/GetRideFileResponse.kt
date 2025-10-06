package com.sajsoft.cork2025.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GetRideFileResponse(
    val rv: Int, // Return value, e.g., ServerConstants.RV_OK
    val msg: String?,
    val filename: String? = null,
    val filePath: String? = null // To store the path where the file is saved locally
)
