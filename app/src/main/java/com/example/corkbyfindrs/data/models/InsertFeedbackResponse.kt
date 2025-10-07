package com.example.corkbyfindrs.data.models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsertFeedbackResponse(
    @SerialName("RV") val rv: Int,
    @SerialName("MSG") val msg: String
)
