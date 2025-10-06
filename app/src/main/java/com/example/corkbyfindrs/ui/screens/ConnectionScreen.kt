package com.example.corkbyfindrs.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.corkbyfindrs.ble.BleViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun ConnectionScreen(bleViewModel: BleViewModel = hiltViewModel()) {
    val connectionState by bleViewModel.connectionState.collectAsState()
    val logs by bleViewModel.logMessages.collectAsState()
    val connectedDevices by bleViewModel.connectedDevices.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        var txt = connectionState
        val devs = connectedDevices.size
        if(devs > 0){
            txt = "${devs} x CONNECTED"
        }
        Text("BLE State: $txt", style = MaterialTheme.typography.titleLarge)
        Text("Connected Devices:")
        connectedDevices.forEach {
            Text("- $it")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Log:")
        LazyColumn(
            modifier = Modifier
                .weight(1f) // Take remaining vertical space
                .fillMaxWidth()
        ) {
            items(logs.takeLast(100)) { log ->
                //val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                Text(
                    text = log,
                    fontSize = 10.sp)
            }
        }
    }
}
