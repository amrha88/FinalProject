package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.ui.components.VehicleActionCard
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AuthViewModel

@Composable
fun VehicleDetailsScreen(
    vehicleId: String,
    viewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onChatbotClick: (String) -> Unit,
    onLicencesClick: (String) -> Unit,
    onNotificationsClick: (String) -> Unit,
    onHistoryClick: (String) -> Unit,
    onAiScannerClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicle = uiState.vehicles.find { it.id == vehicleId }
    
    val title = vehicle?.let {
        listOf(it.manufacturer, it.model, it.year)
            .filter { field -> field.isNotBlank() }
            .joinToString(" ")
    } ?: "Vehicle Details"

    VehicleDetailsContent(
        title = title,
        onBackClick = onBackClick,
        onChatbotClick = { onChatbotClick(vehicleId) },
        onLicencesClick = { onLicencesClick(vehicleId) },
        onNotificationsClick = { onNotificationsClick(vehicleId) },
        onHistoryClick = { onHistoryClick(vehicleId) },
        onAiScannerClick = onAiScannerClick
    )
}

@Composable
fun VehicleDetailsContent(
    title: String,
    onBackClick: () -> Unit,
    onChatbotClick: () -> Unit,
    onLicencesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAiScannerClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF000C1F) // Very dark blue/black background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                // Header bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.Black, shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = title,
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(onClick = { /* Profile click */ }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // AI Warning Scanner Card
                VehicleActionCard(
                    title = "AI Warning Scanner",
                    subtitle = "scan dashboard warning lights",
                    leadingText = "AI",
                    trailingColor = Color(0xFF007BFF),
                    onClick = onAiScannerClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Cards
                VehicleActionCard(
                    title = "Go to chatbot",
                    subtitle = "ask what ever you want",
                    leadingText = "-",
                    trailingColor = Color(0xFFE8EAF6),
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowBack, // Placeholder for chevron-left-like
                    onClick = onChatbotClick,
                    showAlert = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                VehicleActionCard(
                    title = "licence",
                    subtitle = "view all of your licenses",
                    leadingText = "A",
                    trailingColor = Color(0xFF64B5F6),
                    onClick = onLicencesClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                VehicleActionCard(
                    title = "Notifications",
                    subtitle = "get all your notifications",
                    leadingText = "A",
                    trailingColor = Color(0xFF448AFF),
                    onClick = onNotificationsClick,
                    showAlert = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                VehicleActionCard(
                    title = "history",
                    subtitle = "your car history",
                    leadingText = "A",
                    trailingColor = Color(0xFF5E35B1),
                    onClick = onHistoryClick
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom Navigation/Icon
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Live Location Button
                Button(
                    onClick = { /* Live location */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("live location", color = Color.White, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VehicleDetailsPreview() {
    AutomateTheme {
        VehicleDetailsContent(
            title = "Volkswagen Polo 2011",
            onBackClick = {},
            onChatbotClick = {},
            onLicencesClick = {},
            onNotificationsClick = {},
            onHistoryClick = {},
            onAiScannerClick = {}
        )
    }
}
