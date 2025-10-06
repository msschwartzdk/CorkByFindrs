package com.example.corkbyfindrs.ui.theme

import android.Manifest
import android.os.Build


fun getRequiredPermissions(): List<String> {
    return when {
        true -> listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        else -> listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}
