package com.sajsoft.cork2025.data.models

import kotlinx.serialization.Serializable

// For structure, params assembled in ServerClient
@Serializable
data class ChangePasswordRequest(
    val email: String, // From UserSettings
    val password: String, // Current
    val newPassword: String
)
