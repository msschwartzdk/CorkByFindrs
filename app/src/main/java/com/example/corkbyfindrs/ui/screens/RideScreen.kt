package com.example.cork_by_findrs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.corkbyfindrs.components.StatColumn

@Composable
fun RideScreen(
    //state: BleUiState,
) {
    var selectedTab by remember { mutableStateOf("Uge") }

    val tabs = listOf("Dag", "Uge", "Måned")
    val rideData = listOf(
        RideItem("4", "14. okt. 2024", "20. okt. 2024", "11.82 km", "03t 21m", "3.44 km/t"),
        RideItem("1", "7. okt. 2024", "13. okt. 2024", "2.32 km", "00t 44m", "3.13 km/t"),
        RideItem("5", "23. sep. 2024", "29. sep. 2024", "17.82 km", "04t 56m", "3.64 km/t"),
        RideItem("2", "16. sep. 2024", "22. sep. 2024", "5.38 km", "01t 32m", "3.51 km/t"),
        RideItem("3", "9. sep. 2024", "15. sep. 2024", "8.14 km", "02t 15m", "3.20 km/t")
    )

    Column(
        modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF0F0F0)) // Light gray background {
    ){
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ride",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Tab Selector
        Row(
            modifier = Modifier
                .width(250.dp)
                .padding(12.dp)
                .border(
                    width = 1.dp,
                    color = Color.Gray, // Or Color.LightGray for a softer look
                    shape = RoundedCornerShape(30.dp)
                )
                .background(Color(0xFFF0F0F0), RoundedCornerShape(30.dp))
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(if (isSelected) Color(0xFF009CA6) else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else Color.DarkGray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Divider()

        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            items(rideData) { ride ->
                RideItemView(ride)
                //Divider(color = Color.LightGray, thickness = 1.dp)
            }
        }
    }
}

data class RideItem(
    val heartNumber: String,
    val startDate: String,
    val endDate: String,
    val distance: String,
    val duration: String,
    val speed: String
)

@Composable
fun RideItemView(ride: RideItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(16.dp)

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF009CA6), // Or Color.LightGray for a softer look
                        shape = RoundedCornerShape(30.dp)
                    ),
                    //.background(Color(0xFF009CA6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = Color(0xFF009CA6),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = ride.heartNumber,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    //modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = ride.startDate,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black)
                Text(
                    text = ride.endDate,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            StatColumn(title = "Længde", value = ride.distance)
            VerticalDivider()
            StatColumn(title = "Varighed", value = ride.duration)
            VerticalDivider()
            StatColumn(title = "Gns. Fart", value = ride.speed)
        }
    }
}



