package com.example.corkbyfindrs.data.models

import kotlinx.serialization.Serializable

// For structure, params assembled in ServerClient
@Serializable
data class RegisterOwnerRequest(
    val email: String,
    val deviceAddress: String,
    val registrationToken: String // Hex string
)
