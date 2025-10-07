package com.example.corkbyfindrs.data

import java.io.IOException

data class ServerException(
    val httpStatusCode: Int?,
    val errorMessage: String,
    val messageBody: String?,
    val originalException: Throwable? = null
) : IOException(errorMessage, originalException)
