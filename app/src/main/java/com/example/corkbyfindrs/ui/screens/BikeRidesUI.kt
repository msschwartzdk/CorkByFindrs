package com.example.corkbyfindrs.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.corkbyfindrs.data.models.RideInfo

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Helper function to format timestamp string to a more readable date/time
// Example: "1636197926" (seconds) -> "Nov 06, 2021 11:25:26 AM" (local time)
// Or "yyyy-MM-dd HH:mm:ss" -> "Nov 06, 2021 11:25:26 AM"
fun formatRideTimestamp(timestamp: String?): String {
    if (timestamp == null) return "N/A"
    return try {
        // Try parsing as seconds first
        val seconds = timestamp.toLong()
        val date = Date(seconds * 1000) // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault() // Display in local timezone
        sdf.format(date)
    } catch (e: NumberFormatException) {
        // If not a long, try parsing as "yyyy-MM-dd HH:mm:ss" (assuming UTC)
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(timestamp)
            if (date != null) {
                val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.getDefault())
                sdf.timeZone = TimeZone.getDefault() // Display in local timezone
                sdf.format(date)
            } else {
                timestamp // Return original if parsing fails
            }
        } catch (pe: Exception) {
            timestamp // Return original if parsing fails
        }
    }
}


@Composable
fun BikeRidesUI(
    rides: List<RideInfo>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Bike Rides:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (rides.isEmpty()) {
            Text("No rides available for this bike.", color = Color.Gray, modifier = Modifier.padding(8.dp))
        }
        // The LazyColumn is removed. The parent composable (BikeListScreen)
        // will be responsible for laying out the RideListItem components if rides are not empty.
        // This composable now primarily serves as a section header.
    }
}

@Composable
fun RideListItem( // This Composable remains for displaying a single ride item.
    rideInfo: RideInfo,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ride ID: ${rideInfo.rideid ?: "N/A"}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text("Start Time: ${formatRideTimestamp(rideInfo.startPositionTime ?: rideInfo.start_pos_time)}", style = MaterialTheme.typography.bodySmall)
            Text("End Time: ${formatRideTimestamp(rideInfo.endPositionTime ?: rideInfo.end_pos_time)}", style = MaterialTheme.typography.bodySmall)

            rideInfo.distance?.let { Text("Distance: $it", style = MaterialTheme.typography.bodySmall) }
            rideInfo.speed?.let { Text("Speed: $it", style = MaterialTheme.typography.bodySmall) }
            rideInfo.numberOfStops?.let { Text("Stops: $it", style = MaterialTheme.typography.bodySmall) }
            rideInfo.stopDuration?.let { Text("Stop Duration: $it", style = MaterialTheme.typography.bodySmall) }


            if (rideInfo.startPositionLatitude != null && rideInfo.startPositionLongitude != null) {
                Text("Start Location: (${rideInfo.startPositionLatitude}, ${rideInfo.startPositionLongitude})", style = MaterialTheme.typography.bodySmall)
            }
            rideInfo.startPositionAccuracy?.let { Text("Start Accuracy: $it m", style = MaterialTheme.typography.bodySmall) }

            if (rideInfo.endPositionLatitude != null && rideInfo.endPositionLongitude != null) {
                Text("End Location: (${rideInfo.endPositionLatitude}, ${rideInfo.endPositionLongitude})", style = MaterialTheme.typography.bodySmall)
            }
            rideInfo.endPositionAccuracy?.let { Text("End Accuracy: $it m", style = MaterialTheme.typography.bodySmall) }

            rideInfo.rideType?.let { Text("Ride Type: $it", style = MaterialTheme.typography.bodySmall) }
            rideInfo.name?.let { Text("Name: $it", style = MaterialTheme.typography.bodySmall) } // Associated name from ride data if present
            // Add more fields as needed

            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.Button(
                onClick = {
                    if (isDownloaded) {
                        onDeleteClick()
                    } else {
                        onDownloadClick()
                    }
                },
                enabled = !isDownloading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isDownloaded) "Delete Ride File" else "Download Ride File")
            }
        }
    }
}
