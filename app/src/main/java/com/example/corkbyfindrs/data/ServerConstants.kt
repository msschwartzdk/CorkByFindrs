package com.example.corkbyfindrs.data

object ServerConstants {
    const val RV_OK = 0
    const val RV_ERROR_GENERAL = -1
    const val RV_NOT_LOGGED_IN = -100
    const val RV_SESSION_EXPIRED = -101 // Example, adjust as per actual server codes
    const val RV_INVALID_PARAMETERS = -102 // Example
    // Add other RV constants as needed by the application from server documentation

    // Alert States
    const val ALERTSTATE_FOUND = 0
    const val ALERTSTATE_MISSING = 1

    // Bike D2D States
    const val D2D_ACTIVE = 0
    const val D2D_DISABLED = 1
}
