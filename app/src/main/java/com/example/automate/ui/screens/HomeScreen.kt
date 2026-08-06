package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.ui.components.AssistantBanner
import com.example.automate.ui.components.VehicleCard
import com.example.automate.ui.components.WelcomeCard
import com.example.automate.ui.components.rememberProfileImagePicker
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AuthViewModel
import com.example.automate.ui.viewmodel.VehicleUiModel

@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onAddVehicleClick: () -> Unit,
    onVehicleClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onNeedHelpClick: () -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val pickImage = rememberProfileImagePicker(onImagePicked = { viewModel.updateProfilePhoto(it) })

    HomeScreenContent(
        userName = uiState.userName,
        photoBase64 = uiState.userPhotoBase64,
        onAvatarClick = pickImage,
        vehicles = uiState.vehicles.map { vehicle ->
            VehicleUiModel(
                id = vehicle.id,
                manufacturer = vehicle.manufacturer,
                model = vehicle.model,
                year = vehicle.year,
                plate = vehicle.plate,
                isDark = false,
                photoBase64 = vehicle.photoBase64
            )
        },
        onAddVehicleClick = onAddVehicleClick,
        onVehicleClick = onVehicleClick,
        onSettingsClick = onSettingsClick,
        onNeedHelpClick = onNeedHelpClick,
        onLogout = onLogout,
        bottomBar = bottomBar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    userName: String? = null,
    photoBase64: String? = null,
    onAvatarClick: (() -> Unit)? = null,
    vehicles: List<VehicleUiModel> = emptyList(),
    onAddVehicleClick: () -> Unit,
    onVehicleClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    onNeedHelpClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF000C1F), // Dark navy background
        bottomBar = bottomBar,
        topBar = {
            TopAppBar(
                title = { Text("Automate", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000C1F)),
                actions = {
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
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onSettingsClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Log out") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onLogout()
                            }
                        )
                    }
                }
            )
        }
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
                onAddVehicleClick = onAddVehicleClick,
                photoBase64 = photoBase64,
                onAvatarClick = onAvatarClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            AssistantBanner(onClick = onNeedHelpClick)

            Spacer(modifier = Modifier.height(24.dp))

            if (vehicles.isEmpty()) {
                EmptyVehicleState(onAddVehicleClick = onAddVehicleClick)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your vehicles",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${vehicles.size}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                            photoBase64 = vehicle.photoBase64,
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
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No vehicles added yet",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Add your first vehicle to start tracking it",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddVehicleClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add vehicle",
                color = Color.White,
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