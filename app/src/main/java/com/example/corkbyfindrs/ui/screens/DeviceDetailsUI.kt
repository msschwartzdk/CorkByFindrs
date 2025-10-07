package com.example.corkbyfindrs.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.corkbyfindrs.data.models.Device
import com.example.corkbyfindrs.data.models.getDeviceTypeCompat

@Composable
fun DeviceDetailsUI(
    devices: List<Device>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Associated Devices:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (devices.isEmpty()) {
            Text("No devices associated with this bike.", color = Color.Gray, modifier = Modifier.padding(8.dp))
        }
        // The LazyColumn is removed. The parent composable (BikeListScreen)
        // will be responsible for laying out the DeviceItem components if devices are not empty.
        // This composable now primarily serves as a section header.
    }
}

@Composable
fun DeviceItem( // This Composable remains for displaying a single device item.
    device: Device,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = device.name ?: "Device ID: ${device.deviceId}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Address: ${device.address}", style = MaterialTheme.typography.bodySmall)
            Text("Type: ${device.getDeviceTypeCompat()}", style = MaterialTheme.typography.bodySmall)
            device.alertState?.let {
                Text("Alert State: $it", style = MaterialTheme.typography.bodySmall)
            }
            // Add more device details as needed
            // e.g., device.secret, device.maxLogId, etc. but be mindful of what to show.
        }
    }
}
