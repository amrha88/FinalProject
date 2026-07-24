package com.example.automate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VehicleCard(
    manufacturer: String,
    model: String,
    year: String,
    plate: String,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val contentColor = if (isDark) Color.White else Color.Black
    val plateColor = if (isDark) Color.LightGray else Color.Gray

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD7EAD8)), // Light green from design
                contentAlignment = Alignment.Center
            ) {
                // Placeholder image/emoji
                Text("🚗", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$manufacturer $model",
                    color = contentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$year • number:$plate",
                    color = plateColor,
                    fontSize = 13.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isDark) Color.Gray.copy(alpha = 0.6f) else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
