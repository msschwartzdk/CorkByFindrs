package com.example.corkbyfindrs.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun StatColumn(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        //Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontSize = 10.sp,
            color = Color.Black
        )
    }
}