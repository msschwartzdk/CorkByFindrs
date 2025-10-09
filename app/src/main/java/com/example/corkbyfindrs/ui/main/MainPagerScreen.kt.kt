package com.example.corkbyfindrs.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.cork_by_findrs.ui.screens.BikeScreen
import com.example.cork_by_findrs.ui.screens.HomeScreen
import com.example.cork_by_findrs.ui.screens.ProfileScreen
import com.example.cork_by_findrs.ui.screens.RideScreen
import com.example.cork_by_findrs.ui.screens.SettingsScreen
import com.example.corkbyfindrs.ui.screens.BikeDataViewModel
import com.example.corkbyfindrs.ui.screens.BikeListScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    bikeDataViewModel: BikeDataViewModel,
    onOpenLog: () -> Unit // For navigating to the log screen later
) {
    val pagerState = rememberPagerState(initialPage = 3, pageCount = { mainScreens.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(pagerState = pagerState)
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (mainScreens[page]) {
                is MainScreen.Profile -> ProfileScreen()
                is MainScreen.Ride -> RideScreen()
                is MainScreen.Home -> HomeScreen(bikeDataViewModel) // log button here later
                is MainScreen.Bike -> BikeScreen(bikeDataViewModel)
                is MainScreen.Settings -> BikeListScreen(bikeDataViewModel)//SettingsScreen()
            }
        }
    }
}
