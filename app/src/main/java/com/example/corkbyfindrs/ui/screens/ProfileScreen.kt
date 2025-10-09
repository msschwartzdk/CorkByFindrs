package com.example.cork_by_findrs.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.corkbyfindrs.components.InfoRow
import com.example.corkbyfindrs.components.StatColumn
import com.example.corkbyfindrs.components.TabButton

@Composable
fun ProfileScreen(
    //state: BleUiState,
) {
    var showSubMenu by remember { mutableStateOf(false) }
    /*
    if (showSubMenu) {
        ProfileSubMenuScreen(
            onBackClick = { showSubMenu = false },
            onLoginLogoutClick = { email, password -> /* Handle login */ },
            onDeleteProfileClick = { /* Handle delete */ }
        )
    } else {*/

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0)) // Light gray background)
        ) {
            // Top Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                //.padding(16.dp)
            ) {
                // Centered Text
                Text(
                    text = "Profil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )

                // Right-aligned Icon
                IconButton(
                    onClick = { showSubMenu = true },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Selector
            Row(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .height(40.dp)
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(30.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(30.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton("Alt", selected = true)
                TabButton("Uge")
                TabButton("Måned")
                TabButton("År")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Card
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Alt",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Text(
                    "18. feb. 2024 - 20. okt. 2024",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Top Stats Row
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    StatColumn("Distance", "4320 km")
                    VerticalDivider()
                    StatColumn("Varighed", "220t 16m")
                    VerticalDivider()
                    StatColumn("Gns. fart", "21.66 km/t")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Graph placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                ) {
                    // Replace this with your actual chart Composable
                    Text(
                        text = "GRAPH HERE",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Lines
                InfoRow("Kalorier", "121152 kCal")
                HorizontalDivider()
                InfoRow("Antal ture", "213")
                HorizontalDivider()
                InfoRow("Længste tur", "180.58 km")
                HorizontalDivider()
                InfoRow("Hurtigste tur", "253.03 km/t")
            }
        //}
    }
}



