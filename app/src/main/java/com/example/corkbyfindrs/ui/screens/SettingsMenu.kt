package com.example.corkbyfindrs.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider // Changed from HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
fun SettingsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    userEmail: String,
    onLogoutClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Box to anchor the DropdownMenu.
    // In BikeListScreen, the IconButton acts as the anchor, so this Box might be redundant
    // if SettingsMenu is directly placed inside the actions scope of TopAppBar.
    // However, keeping it here for structural clarity of the menu itself.
    // The IconButton in TopAppBar's actions will be the actual anchor.
    Box(modifier = modifier) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true) // Ensure menu can be dismissed by clicking outside
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Logged in as:",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = userEmail.ifEmpty { "N/A" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Divider() // Changed from HorizontalDivider
            DropdownMenuItem(
                text = { Text("Change Password") },
                onClick = {
                    onChangePasswordClick()
                    onDismissRequest() // Dismiss menu after click
                }
            )
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    onLogoutClick()
                    onDismissRequest() // Dismiss menu after click
                }
            )
        }
    }
}
