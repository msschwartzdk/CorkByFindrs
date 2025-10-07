package com.example.corkbyfindrs.ui.theme

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.corkbyfindrs.ble.BleViewModel
import com.example.corkbyfindrs.data.ServerClient
import com.example.corkbyfindrs.data.UserSettingsRepository
import com.example.corkbyfindrs.service.CorkForegroundService
import com.example.corkbyfindrs.ui.screens.BikeDataViewModel
import com.example.corkbyfindrs.ui.screens.BikeListScreen
import com.example.corkbyfindrs.ui.screens.ConnectionScreen
import com.example.corkbyfindrs.ui.screens.LoginScreen
import com.example.corkbyfindrs.ui.screens.PermissionScreen
import com.example.corkbyfindrs.ui.screens.ScannerScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlin.text.Typography.dagger


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private val TAG = "MainActivity"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "*** CorkByFindrs onCreate ***")

        createNotificationChannel(applicationContext)

        setContent {
            StartNavigation()
        }
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            "cork_bike_channel",
            "Cork Bike Scanner",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

@Composable
fun StartNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route



    // Log route changes
    LaunchedEffect(currentRoute) {
        Log.i("NAVIGATION", "Navigated to route: $currentRoute")
    }

    NavHost(navController, startDestination = "permissions") {
        composable("permissions") {
            val viewModel = hiltViewModel<PermissionViewModel>()
            PermissionScreen(
                viewModel = viewModel,
                onPermissionsGranted = {
                    navController.navigate("login") {
                        popUpTo("permissions") { inclusive = true } // Optional: clear backstack
                    }
                }
            )
        }

        composable("login") {
            val loginViewModel = hiltViewModel<LoginViewModel>()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("fetch_bike_data") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("fetch_bike_data") {
            val bikeDataViewModel = hiltViewModel<BikeDataViewModel>()
            val bleViewModel = hiltViewModel<BleViewModel>()
            val context = LocalContext.current
            val started = remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if (!started.value) {
                    val intent = Intent(context, CorkForegroundService::class.java)
                    ContextCompat.startForegroundService(context, intent)
                    started.value = true
                }
            }

            BikeListScreen(
                viewModel = bikeDataViewModel,
            )

            /*
            ConnectionScreen(
                bleViewModel = bleViewModel,
            )*/
        }
    }
}
