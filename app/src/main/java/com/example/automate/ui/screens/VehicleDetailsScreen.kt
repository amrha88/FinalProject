package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.R
import com.example.automate.domain.model.EngineVariant
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
    onEditClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicle = uiState.vehicles.find { it.id == vehicleId }

    val vehicleName = vehicle?.let { "${it.manufacturer} ${it.model}" } ?: stringResource(R.string.vehicle_default_name)
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
        specsLoadFailed = uiState.specsLoadFailed,
        onBackClick = onBackClick,
        onChatbotClick = { onChatbotClick(vehicleId) },
        onLicencesClick = { onLicencesClick(vehicleId) },
        onNotificationsClick = { onNotificationsClick(vehicleId) },
        onHistoryClick = { onHistoryClick(vehicleId) },
        onDocumentsClick = { onDocumentsClick(vehicleId) },
        onAiScannerClick = onAiScannerClick,
        onEditClick = { onEditClick(vehicleId) },
        onDeleteConfirmed = { viewModel.deleteVehicle(vehicleId) },
        onVariantSelected = { variant -> viewModel.selectEngineVariant(vehicleId, variant) },
        bottomBar = bottomBar
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
    specsLoadFailed: Boolean = false,
    onBackClick: () -> Unit,
    onChatbotClick: () -> Unit,
    onLicencesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onDocumentsClick: () -> Unit,
    onAiScannerClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onVariantSelected: (EngineVariant) -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF000C1F), // Dark navy background
        bottomBar = bottomBar,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.vehicle_details_title),
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
                            contentDescription = stringResource(R.string.action_back),
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
                                contentDescription = stringResource(R.string.cd_menu),
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_vehicle_edit_title)) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete_vehicle_action)) },
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
                    onClick = { /* No action needed here */ },
                    compact = true
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
                    VehicleInfoCard(specs, onVariantSelected = onVariantSelected)
                    Spacer(modifier = Modifier.height(24.dp))
                } else if (specsLoadFailed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.vehicle_info_title),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.vehicle_info_unavailable),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = stringResource(R.string.vehicle_services_title),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Service Cards
                val serviceCards: List<@Composable () -> Unit> = listOf(
                    {
                        VehicleFeatureCard(
                            title = stringResource(R.string.feature_ai_scanner_title),
                            subtitle = stringResource(R.string.feature_ai_scanner_subtitle),
                            icon = Icons.Default.CameraAlt,
                            isHighlighted = true,
                            onClick = onAiScannerClick
                        )
                    },
                    {
                        VehicleFeatureCard(
                            title = stringResource(R.string.feature_documents_title),
                            subtitle = stringResource(R.string.feature_documents_subtitle),
                            icon = Icons.Default.Description,
                            highlightLabel = stringResource(R.string.feature_ai_powered_label),
                            onClick = onDocumentsClick
                        )
                    },
                    {
                        VehicleFeatureCard(
                            title = stringResource(R.string.feature_ai_assistant_title),
                            subtitle = stringResource(R.string.feature_ai_assistant_subtitle),
                            icon = Icons.AutoMirrored.Filled.Chat,
                            showAlert = true, // Preserve alert logic if needed
                            onClick = onChatbotClick
                        )
                    },
                    {
                        VehicleFeatureCard(
                            title = stringResource(R.string.notifications_title),
                            subtitle = stringResource(R.string.feature_notifications_subtitle),
                            icon = Icons.Default.Notifications,
                            onClick = onNotificationsClick
                        )
                    },
                    {
                        VehicleFeatureCard(
                            title = stringResource(R.string.feature_history_title),
                            subtitle = stringResource(R.string.feature_history_subtitle),
                            icon = Icons.Default.History,
                            onClick = onHistoryClick
                        )
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    serviceCards.chunked(2).forEach { rowCards ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowCards.forEach { card ->
                                Box(modifier = Modifier.weight(1f)) { card() }
                            }
                            if (rowCards.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_vehicle_confirm_title)) },
            text = { Text(stringResource(R.string.action_cannot_be_undone)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteConfirmed()
                }) {
                    Text(stringResource(R.string.action_delete), color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun VehicleInfoCard(specs: VehicleSpecs, onVariantSelected: (EngineVariant) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.vehicle_info_title),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        if (specs.variants.size > 1) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.variant_multi_engine_hint),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                specs.variants.forEach { variant ->
                    val selected = variant.name == specs.selectedVariantName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Color(0xFF007BFF) else Color.White.copy(alpha = 0.08f))
                            .clickable { onVariantSelected(variant) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = variant.name ?: stringResource(R.string.variant_default_name),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val stats = listOfNotNull(
            specs.fuelConsumptionL100km?.let { stringResource(R.string.spec_fuel_consumption) to "%.1f L/100km".format(it) },
            specs.fuelType?.let { stringResource(R.string.spec_fuel_type) to it },
            specs.engineDisplacementL?.let { stringResource(R.string.spec_engine) to "%.1fL".format(it) },
            specs.horsepower?.let { stringResource(R.string.spec_horsepower) to stringResource(R.string.spec_horsepower_value, it) },
            specs.transmission?.let { stringResource(R.string.label_transmission) to it },
            specs.fuelTankCapacityL?.let { stringResource(R.string.spec_fuel_tank) to "%.0fL".format(it) }
        )

        if (stats.isEmpty()) {
            Text(
                text = stringResource(R.string.vehicle_info_empty),
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

            AiDisclaimerNote(text = stringResource(R.string.ai_specs_disclaimer))
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
