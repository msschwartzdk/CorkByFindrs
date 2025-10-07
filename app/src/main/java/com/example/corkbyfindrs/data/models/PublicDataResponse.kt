package com.example.corkbyfindrs.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublicDataResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String,
    @SerialName("PUBLIC_DATA") val publicData: String? = null, // Base64 encoded
    @SerialName("PUBLIC_DATA_ALLOW_BONDING") val publicDataAllowBonding: String? = null, // Base64 encoded
    @SerialName("PUBLIC_DATA_DEVICE_ID") val publicDataDeviceId: String? = null, // Java parsed as String
    @SerialName("PUBLIC_DATA_DEVICE_ADDRESS") val publicDataDeviceAddress: String? = null
)
