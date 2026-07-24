package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.ui.components.RobotSection
import com.example.automate.ui.components.VehicleCard
import com.example.automate.ui.components.WelcomeCard
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AuthViewModel
import com.example.automate.ui.viewmodel.VehicleUiModel

@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onAddVehicleClick: () -> Unit,
    onVehicleClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        userName = uiState.userName,
        vehicles = uiState.vehicles.map { vehicle ->
            VehicleUiModel(
                id = vehicle.id,
                manufacturer = vehicle.manufacturer,
                model = vehicle.model,
                year = vehicle.year,
                plate = vehicle.plate,
                isDark = false
            )
        },
        onAddVehicleClick = onAddVehicleClick,
        onVehicleClick = onVehicleClick
    )
}

@Composable
fun HomeScreenContent(
    userName: String? = null,
    vehicles: List<VehicleUiModel> = emptyList(),
    onAddVehicleClick: () -> Unit,
    onVehicleClick: (String) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF000C1F) // Dark navy background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            WelcomeCard(
                userName = userName,
                onAddVehicleClick = onAddVehicleClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            RobotSection()

            Spacer(modifier = Modifier.height(24.dp))

            if (vehicles.isEmpty()) {
                EmptyVehicleState(onAddVehicleClick = onAddVehicleClick)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(vehicles) { vehicle ->
                        VehicleCard(
                            manufacturer = vehicle.manufacturer,
                            model = vehicle.model,
                            year = vehicle.year,
                            plate = vehicle.plate,
                            isDark = vehicle.isDark,
                            onClick = { onVehicleClick(vehicle.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EmptyVehicleState(onAddVehicleClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No vehicles added yet",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddVehicleClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add vehicle",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val mockVehicles = listOf(
        VehicleUiModel("1", "Volkswagen", "Polo", "2011", "91-272-30", isDark = false),
        VehicleUiModel("2", "Toyota", "Corolla", "2022", "301-33-444", isDark = true)
    )
    AutomateTheme {
        HomeScreenContent(
            userName = "George",
            vehicles = mockVehicles,
            onAddVehicleClick = {},
            onVehicleClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    AutomateTheme {
        HomeScreenContent(
            userName = null,
            vehicles = emptyList(),
            onAddVehicleClick = {},
            onVehicleClick = {}
        )
    }
}
