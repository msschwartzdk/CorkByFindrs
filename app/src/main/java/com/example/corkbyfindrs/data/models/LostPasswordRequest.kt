package com.example.corkbyfindrs.data.models

import kotlinx.serialization.Serializable

// For structure, params assembled in ServerClient
@Serializable
data class LostPasswordRequest(
    val email: String,
    val newPassword: String // As per original Java implementation
)
