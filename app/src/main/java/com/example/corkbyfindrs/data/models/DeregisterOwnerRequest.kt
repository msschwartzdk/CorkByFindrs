package com.example.corkbyfindrs.data.models

import kotlinx.serialization.Serializable

// For structure, params assembled in ServerClient
@Serializable
data class DeregisterOwnerRequest(
    val email: String, // From UserSettings
    val deviceAddress: String
)
