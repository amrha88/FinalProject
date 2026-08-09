package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
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
import com.example.automate.ui.components.VehicleCard
import com.example.automate.ui.components.VehicleFeatureCard
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
    onDocumentsClick: (String) -> Unit,
    onAiScannerClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicle = uiState.vehicles.find { it.id == vehicleId }

    val vehicleName = vehicle?.let { "${it.manufacturer} ${it.model}" } ?: "Vehicle"
    val vehicleYear = vehicle?.year ?: ""
    val vehiclePlate = vehicle?.plate ?: ""
    val vehiclePhoto = vehicle?.photoBase64

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
        vehicleName = vehicleName,
        vehicleYear = vehicleYear,
        vehiclePlate = vehiclePlate,
        vehiclePhoto = vehiclePhoto,
        specs = vehicle?.specs,
        isLoadingSpecs = uiState.isLoadingSpecs,
        onBackClick = onBackClick,
        onChatbotClick = { onChatbotClick(vehicleId) },
        onLicencesClick = { onLicencesClick(vehicleId) },
        onNotificationsClick = { onNotificationsClick(vehicleId) },
        onHistoryClick = { onHistoryClick(vehicleId) },
        onDocumentsClick = { onDocumentsClick(vehicleId) },
        onAiScannerClick = onAiScannerClick,
        onEditClick = { onEditClick(vehicleId) },
        onDeleteConfirmed = { viewModel.deleteVehicle(vehicleId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailsContent(
    vehicleName: String,
    vehicleYear: String,
    vehiclePlate: String,
    vehiclePhoto: String? = null,
    specs: VehicleSpecs? = null,
    isLoadingSpecs: Boolean = false,
    onBackClick: () -> Unit,
    onChatbotClick: () -> Unit,
    onLicencesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onDocumentsClick: () -> Unit,
    onAiScannerClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF000C1F), // Dark navy background
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Vehicle details",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit vehicle") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete vehicle") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Vehicle Summary Card
                val manufacturer = vehicleName.split(" ").firstOrNull() ?: ""
                val model = vehicleName.split(" ").drop(1).joinToString(" ")
                
                VehicleCard(
                    manufacturer = manufacturer,
                    model = model,
                    year = vehicleYear,
                    plate = vehiclePlate,
                    isDark = false, // White card as per HomeScreen
                    photoBase64 = vehiclePhoto,
                    onClick = { /* No action needed here */ }
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoadingSpecs) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF007BFF), modifier = Modifier.size(32.dp))
                    }
                } else if (specs != null) {
                    VehicleInfoCard(specs)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = "Vehicle services",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Service Cards
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    VehicleFeatureCard(
                        title = "AI Warning Scanner",
                        subtitle = "Scan dashboard warning lights",
                        icon = Icons.Default.CameraAlt,
                        isHighlighted = true,
                        onClick = onAiScannerClick
                    )

                    VehicleFeatureCard(
                        title = "Documents",
                        subtitle = "Upload and manage vehicle documents",
                        icon = Icons.Default.Description,
                        highlightLabel = "AI powered",
                        onClick = onDocumentsClick
                    )

                    VehicleFeatureCard(
                        title = "AI Assistant",
                        subtitle = "Ask about your vehicle",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        showAlert = true, // Preserve alert logic if needed
                        onClick = onChatbotClick
                    )

                    VehicleFeatureCard(
                        title = "Notifications",
                        subtitle = "Maintenance and licence reminders",
                        icon = Icons.Default.Notifications,
                        onClick = onNotificationsClick
                    )

                    VehicleFeatureCard(
                        title = "Vehicle history",
                        subtitle = "View maintenance and service history",
                        icon = Icons.Default.History,
                        onClick = onHistoryClick
                    )
                }
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
            .clip(RoundedCornerShape(24.dp))
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
            vehicleName = "Volkswagen Polo",
            vehicleYear = "2011",
            vehiclePlate = "91-272-30",
            onBackClick = {},
            onChatbotClick = {},
            onLicencesClick = {},
            onNotificationsClick = {},
            onHistoryClick = {},
            onDocumentsClick = {},
            onAiScannerClick = {},
            onEditClick = {},
            onDeleteConfirmed = {}
        )
    }
}
