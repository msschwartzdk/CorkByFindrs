package com.example.corkbyfindrs.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.corkbyfindrs.ui.theme.Cork_Blue
import com.example.corkbyfindrs.ui.theme.Cork_Gray
import com.example.corkbyfindrs.ui.theme.Cork_White
import kotlinx.coroutines.launch

@Composable
fun BottomNavigationBar(
    pagerState: PagerState
) {
    val coroutineScope = rememberCoroutineScope()

    NavigationBar(
        containerColor = Cork_Blue
    ) {
        mainScreens.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        color = Cork_White
                    )
                },
                selected = pagerState.currentPage == index,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Cork_White,
                    selectedTextColor = Cork_White,
                    unselectedIconColor = Cork_Gray,
                    unselectedTextColor = Cork_Gray
                ),
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}


/*
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        MainScreen.Profile,
        MainScreen.Ride,
        MainScreen.Home,
        MainScreen.Bike,
        MainScreen.Settings
    )

    NavigationBar( // ✅ Use NavigationBar instead of BottomNavigation
        containerColor = Cork_Blue, // Background color
        //tonalElevation = 8.dp // Elevation (optional)
    ) {
        val currentRoute = navController.currentDestination?.route

        items.forEach { screen ->
            NavigationBarItem( // ✅ Use NavigationBarItem instead of BottomNavigationItem
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 9.sp,
                        color = Cork_White
                    )
                },
                selected = currentRoute == screen.route,
                colors = NavigationBarItemDefaults.colors( // ✅ Customize selected & unselected colors
                    selectedIconColor = Cork_White,
                    selectedTextColor = Cork_White,
                    unselectedIconColor = Cork_Gray,
                    unselectedTextColor = Cork_Gray
                ),
                onClick = {
                    navController.navigate(screen.route)
                }
            )
        }
    }
}*/