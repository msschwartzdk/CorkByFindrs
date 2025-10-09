package com.example.corkbyfindrs.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MainScreen(val route: String, val title: String, val icon: ImageVector) {
    object Profile : MainScreen("profile", "Profil", Icons.Default.Person)
    object Ride : MainScreen("ride", "Ride", Icons.Default.Favorite)
    object Home : MainScreen("home", "Hjem", Icons.Default.Home)
    object Bike : MainScreen("bike", "Cykel", Icons.Default.PedalBike)
    object Settings : MainScreen("settings", "Indstillinger", Icons.Default.Settings)
}

val mainScreens = listOf(
    MainScreen.Profile,
    MainScreen.Ride,
    MainScreen.Home,
    MainScreen.Bike,
    MainScreen.Settings
)