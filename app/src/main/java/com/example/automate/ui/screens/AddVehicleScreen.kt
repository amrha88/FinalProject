package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.domain.model.CarCatalog
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.AutocompleteTextField
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.viewmodel.AuthViewModel

@Composable
fun AddVehicleScreen(
    viewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.vehicleSaved) {
        if (uiState.vehicleSaved) {
            viewModel.clearVehicleSaved()
            onSaveSuccess()
        }
    }

    AddVehicleScreenContent(
        isSaving = uiState.isSavingVehicle,
        serverError = uiState.vehicleError,
        onBackClick = onBackClick,
        onSave = { manufacturer, model, year, plate ->
            viewModel.clearVehicleError()
            viewModel.addVehicle(manufacturer, model, year, plate)
        }
    )
}

@Composable
fun AddVehicleScreenContent(
    isSaving: Boolean = false,
    serverError: String? = null,
    onBackClick: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var manufacturer by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val displayedError = validationError ?: serverError
    val hasPreview = manufacturer.isNotBlank() || model.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000C1F))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF4FC3F7), Color(0xFF007BFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Add new vehicle",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tell us about your car",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (hasPreview) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFB9E4C9), Color(0xFF8FD3A8))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚗", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = listOf(manufacturer, model)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                            .ifBlank { "Your vehicle" },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = listOfNotNull(
                            year.ifBlank { null },
                            plate.ifBlank { null }?.let { "number:$it" }
                        ).joinToString(" • ").ifBlank { "Fill in the details below" },
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .padding(20.dp)
        ) {
            Text(
                text = "Vehicle details",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutocompleteTextField(
                value = manufacturer,
                onValueChange = { newValue ->
                    if (newValue != manufacturer) {
                        model = ""
                    }
                    manufacturer = newValue
                    validationError = null
                },
                label = "Manufacturer",
                options = CarCatalog.manufacturers,
                placeholder = "e.g. Toyota"
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutocompleteTextField(
                value = model,
                onValueChange = { newValue ->
                    if (newValue != model) {
                        year = ""
                    }
                    model = newValue
                    validationError = null
                },
                label = "Model",
                options = CarCatalog.modelsFor(manufacturer),
                placeholder = "e.g. Corolla"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AutocompleteTextField(
                    value = year,
                    onValueChange = { year = it; validationError = null },
                    label = "Year",
                    options = CarCatalog.yearsFor(manufacturer, model),
                    placeholder = "e.g. 2020",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )

                AppTextField(
                    value = plate,
                    onValueChange = { plate = it; validationError = null },
                    label = "Plate",
                    placeholder = "AB-123-CD",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        displayedError?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (isSaving) {
            CircularProgressIndicator(color = Color(0xFF007BFF))
            Spacer(modifier = Modifier.height(16.dp))
        }

        PrimaryButton(
            text = "Save",
            enabled = !isSaving,
            onClick = {
                if (manufacturer.isBlank() || model.isBlank() || year.isBlank() || plate.isBlank()) {
                    validationError = "All fields are required"
                } else {
                    onSave(manufacturer, model, year, plate)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AddVehicleScreenPreview() {
    AddVehicleScreenContent(
        onBackClick = {},
        onSave = { _, _, _, _ -> }
    )
}