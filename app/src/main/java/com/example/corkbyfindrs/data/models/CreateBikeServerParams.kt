package com.example.corkbyfindrs.data.models

import kotlinx.serialization.Serializable

// Represents parameters for creating a bike, not necessarily a direct request body class
// if params are sent individually. For structure.
@Serializable
data class CreateBikeServerParams( // Renamed to avoid confusion if not a direct body
    val email: String, // From UserSettings
    val sessionId: String, // From UserSettings
    val bikeName: String,
    val iconId: Int
)
