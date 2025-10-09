package com.example.cork_by_findrs.ui.screens
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.corkbyfindrs.R
import com.example.corkbyfindrs.data.models.Bike
import com.example.corkbyfindrs.ui.screens.BikeDataViewModel
import com.example.corkbyfindrs.ui.screens.BikeItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BikeScreen(
    bikeDataViewModel: BikeDataViewModel
) {
    val bikes by bikeDataViewModel.userBikes.collectAsState()
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
                text = "Cykel",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            //contentPadding = padding,
            //modifier = Modifier
            //    .fillMaxSize()
                //.background(Color(0xFFF6F6F6))
        ) {
            items(bikes) { bike ->
                BikeItem(bike)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}


@Composable
fun BikeItem(bike: Bike) {
    var expanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            //.padding(12.dp)
            .background(Color.White, shape = RoundedCornerShape(10.dp))
            //.clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier
                .weight(1f)
                .clickable { expanded = !expanded }) {
                Text(bike.name, fontWeight = FontWeight.SemiBold, color = Color.Black)

                val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                //val lastSeen = formatter.format(Date(lastSeen))
                val timeTxt = "Unknown"
                Text("Sidst set: " + timeTxt, color = Color.Gray, fontSize = 12.sp)
            }


            Icon(
                imageVector = Icons.Default.PedalBike,
                contentDescription = "BikeType",
                modifier = Modifier.size(32.dp)
            )
        }

        if (!expanded) {
            Icon(
                //imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Expand",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { expanded = !expanded }
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            // Placeholder map image
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

            Column {
                ActionButton("Rutevejledning"){
                    Toast.makeText(context, "Rutevejledning not implemented!", Toast.LENGTH_SHORT).show()
                }
                ActionButton("Meld forsvundet", Color.Red){
                    Toast.makeText(context, "Meld forsvundet not implemented!", Toast.LENGTH_SHORT).show()
                }
                ActionButton("Rediger Cykel"){
                    Toast.makeText(context, "Rediger cykel not implemented!", Toast.LENGTH_SHORT).show()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Icon(
                //imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                imageVector = Icons.Default.ExpandLess,
                contentDescription = "Expand",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { expanded = !expanded }
            )

        }
    }
}

@Composable
fun ActionButton(
    text: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
            .clickable { onClick() } // 👈 Makes it clickable
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = 14.sp)
    }
}




