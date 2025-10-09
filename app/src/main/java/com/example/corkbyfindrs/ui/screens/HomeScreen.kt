package com.example.cork_by_findrs.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.dp

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.corkbyfindrs.R
import com.example.corkbyfindrs.data.models.RideInfo
import com.example.corkbyfindrs.ui.screens.BikeDataViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun HomeScreen(
    bikeDataViewModel: BikeDataViewModel
) {
    val rides by bikeDataViewModel.ridesForSelectedBike.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0)) // Light gray background
            //.padding(16.dp)
    ) {
        // Headline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Hjem",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ride list
        if(rides.isEmpty()){

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)

                    //.background(Color(0xFFF0F0F0)) // Light gray background
                    //.padding(16.dp)
                ) {

                    Text(
                        text = "Her vises dine kommende cykelture.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        //modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        imageVector = Icons.Default.PedalBike,
                        contentDescription = "NoRideDataIcon",
                        modifier = Modifier.size(28.dp)
                    )
                }

            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(rides) { ride ->
                RideCard(ride = ride)
            }
        }
    }
}


@Composable
fun RideCard(ride: RideInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp)) // White background
            .padding(16.dp)
            //.shadow(2.dp, RoundedCornerShape(12.dp)) // optional: soft shadow
    ) {
        // Title and timestamp
        val titleTxt = "Søndag morgen"
        Text(titleTxt, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)

        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val timeTxt = formatter.format(Date(ride.startPositionTime))

        Text(timeTxt, fontSize = 12.sp, color = Color.Gray)
        val location = "UNKNOWN"
        Text(location, fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        // Static map image (replace with mapview later if needed)
        Image(
            painter = painterResource(id = R.drawable.sample_map), // placeholder image
            contentDescription = "Map",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stats row
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            RideStat("Distance", ride.distance ?: "Unknown")
            RideStat("Tid", ride.endPositionTime ?: "Unknown")
            RideStat("Energi", ride.ride_ahku ?: "--")
            RideStat("Gns. Fart", ride.speed ?: "--")
        }
    }
}

@Composable
fun RideStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
    }
}
/*
@Composable
fun HomeScreen(
    state: BleUiState,
) {
    val context = LocalContext.current
    val itemsList = remember { mutableStateListOf("Item 1", "Item 2", "Item 3", "Item 1", "Item 2", "Item 3", "Item 1", "Item 2", "Item 3") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cork_LightGray),
            //.background(Cork_Blue),
        //verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Box with white background and centered text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp) // Adjust the height as needed
                .background(Color.White),
            contentAlignment = Alignment.Center // Centers content inside the Box
        ) {
            Text(
                text = "Hjem",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Scrollable List Below the Top Box
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            items(itemsList) { item ->
                HomeViewItem(item)
            }
        }
    }
}

@Composable
fun HomeViewItem(item: String){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(vertical = 8.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center // Centers content inside the Box
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            // Top row with two boxes. Left contains text. Right contains an icon
            Row(
                modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(Color.White),
                horizontalArrangement = Arrangement.SpaceEvenly

            ){
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        //.padding(8.dp)
                        //.background(Color.Gray),
                    //contentAlignment = Alignment.CenterStart
                ){
                    Column(
                        //modifier = Modifier
                            //.height(120.dp)
                            //.background(Color.LightGray),
                        verticalArrangement = Arrangement.SpaceEvenly
                        //verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Søndag Morgen",
                            color = Cork_Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "20. april 2025 kl. 08.15.44 CEST",
                            color = Cork_Gray,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "Gyvelvej, Regstrup, Danmark",
                            color = Cork_Gray,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
                // Right part of top row - Bike icon
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight() ,// Adjust the height as needed
                        //.padding(vertical = 8.dp)
                        //.size(36.dp)
                        //.background(Color.Gray),
                    contentAlignment = Alignment.Center // Centers content inside the Box
                ){
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.PedalBike,
                        contentDescription = "Bike",
                        tint = Cork_Black
                    )
                }
            }

            // Box for showing map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    //.height(260.dp)
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .background(Color.White),
                //contentAlignment = Alignment.CenterStart
            ) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(55.6689938174203, 11.62451853280266), 10f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                )

            }

            //Bottom row showing 4 boxes with ride info
            val rideInfoTxtSize = 12.sp
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .fillMaxWidth()
                    //.padding(vertical = 8.dp)
                    .background(Color.White),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically

            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Distance",
                            color = Cork_Gray,
                            fontSize = rideInfoTxtSize,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "4.37 km",
                            color = Cork_Black,
                            fontSize = rideInfoTxtSize,
                            lineHeight = 14.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tid",
                            color = Cork_Gray,
                            fontSize = rideInfoTxtSize,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "1t 10m",
                            color = Cork_Black,
                            fontSize = rideInfoTxtSize,
                            lineHeight = 14.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        //.padding(8.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        //modifier = Modifier
                        //.height(120.dp)
                        //.background(Color.LightGray),
                        //verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Energi",
                            color = Cork_Gray,
                            fontSize = rideInfoTxtSize,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "565 kCal",
                            color = Cork_Black,
                            fontSize = rideInfoTxtSize,
                            lineHeight = 14.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        //.padding(8.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        //modifier = Modifier
                        //.height(120.dp)
                        //.background(Color.LightGray),
                        //verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        Text(
                            text = "Gns. fart",
                            color = Cork_Gray,
                            fontSize = rideInfoTxtSize,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "4.24 km/t",
                            color = Cork_Black,
                            fontSize = rideInfoTxtSize,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

        }

    }

}*/