package com.example.corkbyfindrs.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // Use itemsIndexed if index is needed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.corkbyfindrs.data.models.BikeLogEntry


@Composable
fun BikeLogsUI(
    logs: List<BikeLogEntry>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Bike Logs:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (logs.isEmpty()) {
            Text("No logs available for this bike.", color = Color.Gray, modifier = Modifier.padding(8.dp))
        }
        // The LazyColumn is removed. The parent composable (BikeListScreen)
        // will be responsible for laying out the BikeLogItem components if logs are not empty.
        // This composable now primarily serves as a section header.
        // If logs are not empty, the parent will iterate and call BikeLogItem.
    }
}

@Composable
fun BikeLogItem( // This Composable remains for displaying a single log item.
    logEntry: BikeLogEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Log Time: ${logEntry.logTime ?: "N/A"}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Device ID: ${logEntry.deviceId}", style = MaterialTheme.typography.bodySmall)
            logEntry.reporterId?.let {
                Text("Reporter ID: $it", style = MaterialTheme.typography.bodySmall)
            }
            logEntry.address?.let {
                Text("Device Address: $it", style = MaterialTheme.typography.bodySmall)
            }
            if (logEntry.latitude != null && logEntry.longitude != null) {
                Text("Location: (${logEntry.latitude}, ${logEntry.longitude})", style = MaterialTheme.typography.bodySmall)
            }
            logEntry.accuracy?.let {
                Text("Accuracy: $it m", style = MaterialTheme.typography.bodySmall)
            }
            logEntry.rssi?.let {
                Text("RSSI: $it dBm", style = MaterialTheme.typography.bodySmall)
            }
            logEntry.temperature?.let {
                Text("Temperature: $it °C", style = MaterialTheme.typography.bodySmall)
            }
            logEntry.parked?.let {
                Text("Parked: ${if (it == 1) "Yes" else "No"}", style = MaterialTheme.typography.bodySmall)
            }
            logEntry.alert?.let {
                Text("Alert: ${if (it == 1) "Active" else "Inactive"}", style = MaterialTheme.typography.bodySmall)
            }
            logEntry.battVoltage?.let {
                Text("Battery Voltage: $it mV", style = MaterialTheme.typography.bodySmall) // Assuming mV
            }
            logEntry.battCritical?.let {
                Text("Battery Critical: ${if (it == 1) "Yes" else "No"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
