package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.domain.model.VehicleSpecs
import com.example.automate.ui.components.AiDisclaimerNote
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
    onAiScannerClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicle = uiState.vehicles.find { it.id == vehicleId }

    val title = vehicle?.let {
        listOf(it.manufacturer, it.model, it.year)
            .filter { field -> field.isNotBlank() }
            .joinToString(" ")
    } ?: "Vehicle Details"

    LaunchedEffect(uiState.vehicleDeleted) {
        if (uiState.vehicleDeleted) {
            viewModel.clearVehicleDeleted()
            onBackClick()
        }
    }

    LaunchedEffect(vehicleId) {
        viewModel.loadSpecsIfNeeded(vehicleId)
    }

    VehicleDetailsContent(
        title = title,
        specs = vehicle?.specs,
        isLoadingSpecs = uiState.isLoadingSpecs,
        onBackClick = onBackClick,
        onChatbotClick = { onChatbotClick(vehicleId) },
        onLicencesClick = { onLicencesClick(vehicleId) },
        onNotificationsClick = { onNotificationsClick(vehicleId) },
        onHistoryClick = { onHistoryClick(vehicleId) },
        onAiScannerClick = onAiScannerClick,
        onEditClick = { onEditClick(vehicleId) },
        onDeleteConfirmed = { viewModel.deleteVehicle(vehicleId) }
    )
}

@Composable
fun VehicleDetailsContent(
    title: String,
    specs: VehicleSpecs? = null,
    isLoadingSpecs: Boolean = false,
    onBackClick: () -> Unit,
    onChatbotClick: () -> Unit,
    onLicencesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAiScannerClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteConfirmed: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
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

                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Vehicle options",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit vehicle") },
                                    onClick = {
                                        menuExpanded = false
                                        onEditClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete vehicle") },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoadingSpecs && specs == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF007BFF))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else if (specs != null) {
                    VehicleInfoCard(specs)
                    Spacer(modifier = Modifier.height(24.dp))
                }

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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete vehicle?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteConfirmed()
                }) {
                    Text("Delete", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun VehicleInfoCard(specs: VehicleSpecs) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(20.dp)
    ) {
        Text(
            text = "Vehicle info",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        val stats = listOfNotNull(
            specs.fuelConsumptionL100km?.let { "Fuel consumption" to "%.1f L/100km".format(it) },
            specs.fuelType?.let { "Fuel type" to it },
            specs.engineDisplacementL?.let { "Engine" to "%.1fL".format(it) },
            specs.horsepower?.let { "Horsepower" to "$it hp" },
            specs.transmission?.let { "Transmission" to it },
            specs.fuelTankCapacityL?.let { "Fuel tank" to "%.0fL".format(it) }
        )

        if (stats.isEmpty()) {
            Text(
                text = "Vehicle info isn't available for this model yet.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        } else {
            stats.chunked(2).forEach { rowStats ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowStats.forEach { (label, value) ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            Text(
                                text = value,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (rowStats.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            AiDisclaimerNote(text = "AI-estimated specs. It can make mistakes; actual figures vary by trim.")
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
