package com.example.corkbyfindrs.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.unit.dp
import com.example.corkbyfindrs.ui.theme.PermissionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel,
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val permissionState by viewModel.permissionState.collectAsState()
    val deniedPermissions by viewModel.deniedPermissions.collectAsState()

    val foregroundPermissions = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
        )
    )
    val backgroundPermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    val foregroundGranted = foregroundPermissions.permissions.all { it.status.isGranted }
    val backgroundGranted = backgroundPermission.status.isGranted

    // Ask for permissions on first launch
    LaunchedEffect(Unit) {
        if (!foregroundGranted) {
            foregroundPermissions.launchMultiplePermissionRequest()
        }
        else if (!backgroundGranted){
            backgroundPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(foregroundGranted) {
        if (foregroundGranted) {
            Log.d("PERMISSION", "Foreground Permissions granted!")
            if (!backgroundGranted){
                backgroundPermission.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(foregroundGranted and backgroundGranted){
        if (foregroundGranted and backgroundGranted) {
            Log.d("PERMISSION", "Foreground AND background Permissions granted — All good!")
            onPermissionsGranted()
        }
    }

    /*
    // Launcher for requesting multiple permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.checkPermissions() // Re-evaluate after user interaction
    }

    // Trigger permission check on first load
    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    Log.i("Permission","Permisstion State: ${permissionState}")
    if (permissionState) {
        onPermissionsGranted()
        return
    }
    */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app requires Bluetooth and Location permissions to detect nearby devices, even in the background."
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                //permissionLauncher.launch(getRequiredPermissions().toTypedArray())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permissions")
        }

        // Optional: Handle permanently denied permissions
        if (deniedPermissions.any()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Some permissions were denied permanently. Please enable them manually in app settings.",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    //Open system settings for this app
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open App Settings")
            }
        }
    }
}