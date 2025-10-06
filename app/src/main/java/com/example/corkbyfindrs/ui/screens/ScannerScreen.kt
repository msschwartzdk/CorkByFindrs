package com.example.corkbyfindrs.ui.screens

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.corkbyfindrs.ble.BeaconScannerManager
import com.example.corkbyfindrs.service.CorkForegroundService

@Composable
fun ScannerScreen(app: Application) {
    val started = remember { mutableStateOf(false) }

    LaunchedEffect(started.value) {
        if (!started.value) {
            //Start foreground service including the beacon scanner!
            val intent = Intent(app, CorkForegroundService::class.java)
            ContextCompat.startForegroundService(app, intent)
            started.value = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Scanning for beacons…")
    }
}
