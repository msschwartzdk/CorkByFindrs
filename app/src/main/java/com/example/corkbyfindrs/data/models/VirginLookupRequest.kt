package com.sajsoft.cork2025.data.models

import kotlinx.serialization.Serializable

// Not strictly needed as a request body, but good for structure.
// Parameters will be assembled in the ServerClient function.
@Serializable
data class VirginLookupRequest(
    val email: String,
    val deviceAddress: String,
    val deviceToken: String // Hex string
)
