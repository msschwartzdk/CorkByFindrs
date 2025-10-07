package com.example.corkbyfindrs.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeListScreen(
    viewModel: BikeDataViewModel
) {
    val userBikes by viewModel.userBikes.collectAsState()
    val selectedBike by viewModel.selectedBike.collectAsState()
    val devicesForSelectedBike by viewModel.devicesForSelectedBike.collectAsState()
    val logsForSelectedBike = viewModel.getLogsForSelectedBike()
    val ridesForSelectedBike by viewModel.ridesForSelectedBike.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val showChangePasswordDialog by viewModel.showChangePasswordDialog.collectAsState()
    val changePasswordMessage by viewModel.changePasswordMessage.collectAsState()
    val isChangingPassword by viewModel.isChangingPassword.collectAsState()
    // val errorMessage by viewModel.errorMessage.collectAsState() // Original general error
    val generalErrorMessage by viewModel.errorMessage.collectAsState() // For general screen errors (if any)

    // States for the download/delete error dialog
    val showErrorDialogState by viewModel.showErrorDialog.collectAsState()
    val dialogErrorMessage by viewModel.errorMessage.collectAsState() // Reusing errorMessage for dialog

    var showSettingsMenu by remember { mutableStateOf(false) }

    val onLogoutClicked = {
        showSettingsMenu = false
        viewModel.performLogout()
    }
    val onChangePasswordClicked = {
        showSettingsMenu = false
        viewModel.openChangePasswordDialog()
    }

    Log.i("BikeListScreen","BikeListScreen composed!")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bike Details") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = { showSettingsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    SettingsMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { showSettingsMenu = false },
                        userEmail = userEmail,
                        onLogoutClick = onLogoutClicked,
                        onChangePasswordClick = onChangePasswordClicked
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
            // horizontalAlignment = Alignment.CenterHorizontally // Removed for testing
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            if (userBikes.isEmpty() && selectedBike == null) {
                item { Text("Loading bike data or no bikes found.", color = Color.Gray) }
            } else {
                item {
                    BikeSelectionUI(
                        bikes = userBikes,
                        selectedBike = selectedBike,
                        onBikeSelected = { bike -> viewModel.selectBike(bike) },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                selectedBike?.let { _ ->
                    // Associated Devices Section
                    item {
                        DeviceDetailsUI(devices = devicesForSelectedBike) // Header
                    }
                    if (devicesForSelectedBike.isNotEmpty()) {
                        // Using the simplest overload of itemsIndexed for diagnostics
                        itemsIndexed(devicesForSelectedBike) { index, device ->
                            DeviceItem(device = device, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }


                    // Bike Logs Section
                    item {
                        BikeLogsUI(logs = logsForSelectedBike) // Header
                    }
                    if (logsForSelectedBike.isNotEmpty()) {
                        itemsIndexed(logsForSelectedBike, key = { index, log -> "${log.logTime ?: ""}_${log.deviceId ?: log.hashCode()}_$index" }) { _, logEntry -> // Use index in key
                            BikeLogItem(logEntry = logEntry, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Bike Rides Section
                    item {
                        BikeRidesUI(rides = ridesForSelectedBike) // Header
                    }
                    if (ridesForSelectedBike.isNotEmpty()) {
                        itemsIndexed(ridesForSelectedBike, key = { index, ride -> ride.rideid ?: ride.startPositionTime ?: ride.hashCode().toString() + "_$index" }) { _, rideInfo ->
                            RideListItem(
                                rideInfo = rideInfo,
                                isDownloaded = rideInfo.localFilePath != null,
                                isDownloading = rideInfo.isDownloading,
                                onDownloadClick = {
                                    rideInfo.rideid?.let { viewModel.downloadRideFile(it) }
                                },
                                onDeleteClick = {
                                    rideInfo.rideid?.let { viewModel.deleteRideFile(it) }
                                },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                } ?: run {
                    if (userBikes.isNotEmpty()) {
                        item { Text("Select a bike to see details.", color = Color.Gray) }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // General error message display (if any, distinct from dialog)
            generalErrorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        // Dialogs should be outside LazyColumn to overlay the screen
        if (showErrorDialogState) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissErrorDialog() },
                title = { Text("Error") },
                text = { Text(dialogErrorMessage ?: "An unknown error occurred.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissErrorDialog() }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showChangePasswordDialog) {
            ChangePasswordDialog(
                showDialog = true,
                message = changePasswordMessage,
                isChangingPassword = isChangingPassword,
                onDismissRequest = { viewModel.closeChangePasswordDialog() },
                onChangePasswordSubmit = { current, new, confirm ->
                    if (new.length < 6) {
                        viewModel.setChangePasswordMessage("Error: New password must be at least 6 characters.")
                        return@ChangePasswordDialog
                    }
                    if (new != confirm) {
                        return@ChangePasswordDialog
                    }
                    viewModel.changePassword(current, new)
                },
                onClearMessage = { viewModel.setChangePasswordMessage(null) }
            )
        }
    }
}