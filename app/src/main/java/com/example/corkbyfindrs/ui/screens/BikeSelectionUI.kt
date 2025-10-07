package com.example.corkbyfindrs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.corkbyfindrs.data.models.Bike


@Composable
fun BikeSelectionUI(
    bikes: List<Bike>,
    selectedBike: Bike?,
    onBikeSelected: (Bike) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bikes.isEmpty()) {
        Text("No bikes available.", color = Color.Gray, modifier = modifier.padding(8.dp))
        return
    }

    Column(modifier = modifier) {
        Text(
            "Select a Bike:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(bikes, key = { it.id }) { bike ->
                BikeItem(
                    bike = bike,
                    isSelected = bike.id == selectedBike?.id,
                    onClicked = { onBikeSelected(bike) }
                )
            }
        }
    }
}

@Composable
fun BikeItem(
    bike: Bike,
    isSelected: Boolean,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(min = 100.dp, max = 150.dp)
            .clickable(onClick = onClicked), // Corrected typo: onClick to onClicked
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bike.name.ifEmpty { "Bike ID: ${bike.id}" },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
