package com.example.automate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import com.example.automate.ui.components.AvatarImage
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.components.rememberProfileImagePicker
import com.example.automate.ui.viewmodel.AuthViewModel

@Composable
fun AddVehicleScreen(
    viewModel: AuthViewModel,
    editingVehicleId: String? = null,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    onDeleted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val editingVehicle = editingVehicleId?.let { id -> uiState.vehicles.find { it.id == id } }
    var carPhoto by remember { mutableStateOf(editingVehicle?.photoBase64) }

    LaunchedEffect(uiState.vehicleSaved) {
        if (uiState.vehicleSaved) {
            viewModel.clearVehicleSaved()
            onSaveSuccess()
        }
    }

    LaunchedEffect(uiState.vehicleDeleted) {
        if (uiState.vehicleDeleted) {
            viewModel.clearVehicleDeleted()
            onDeleted()
        }
    }

    val pickCarPhoto = rememberProfileImagePicker(onImagePicked = { carPhoto = it })

    AddVehicleScreenContent(
        isSaving = uiState.isSavingVehicle,
        serverError = uiState.vehicleError,
        isEditing = editingVehicle != null,
        initialManufacturer = editingVehicle?.manufacturer ?: "",
        initialModel = editingVehicle?.model ?: "",
        initialYear = editingVehicle?.year ?: "",
        initialPlate = editingVehicle?.plate ?: "",
        onBackClick = onBackClick,
        onPickPhotoClick = pickCarPhoto,
        carPhotoBase64 = carPhoto,
        onSave = { manufacturer, model, year, plate ->
            viewModel.clearVehicleError()
            if (editingVehicle != null) {
                viewModel.updateVehicle(editingVehicle.id, manufacturer, model, year, plate, carPhoto)
            } else {
                viewModel.addVehicle(manufacturer, model, year, plate, carPhoto)
            }
        },
        onDeleteClick = if (editingVehicle != null) {
            { viewModel.deleteVehicle(editingVehicle.id) }
        } else null
    )
}

@Composable
fun AddVehicleScreenContent(
    isSaving: Boolean = false,
    serverError: String? = null,
    isEditing: Boolean = false,
    initialManufacturer: String = "",
    initialModel: String = "",
    initialYear: String = "",
    initialPlate: String = "",
    onBackClick: () -> Unit,
    onPickPhotoClick: (() -> Unit)? = null,
    carPhotoBase64: String? = null,
    onSave: (String, String, String, String) -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    var manufacturer by remember { mutableStateOf(initialManufacturer) }
    var model by remember { mutableStateOf(initialModel) }
    var year by remember { mutableStateOf(initialYear) }
    var plate by remember { mutableStateOf(initialPlate) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val displayedError = validationError ?: serverError
    val hasPreview = manufacturer.isNotBlank() || model.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000C1F))
            .statusBarsPadding()
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
                    text = if (isEditing) "Edit vehicle" else "Add new vehicle",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEditing) "Update your car's details" else "Tell us about your car",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (onPickPhotoClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(96.dp)
                    .clickable(onClick = onPickPhotoClick)
            ) {
                if (carPhotoBase64 != null) {
                    AvatarImage(
                        photoBase64 = carPhotoBase64,
                        name = null,
                        size = 96.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF007BFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Add car photo",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (hasPreview) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (carPhotoBase64 != null) {
                    AvatarImage(
                        photoBase64 = carPhotoBase64,
                        name = null,
                        size = 48.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF4FC3F7), Color(0xFF0057D8))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
                    modifier = Modifier.weight(0.4f)
                )

                AppTextField(
                    value = plate,
                    onValueChange = { newValue ->
                        plate = newValue
                            .uppercase()
                            .filter { it.isLetterOrDigit() || it == '-' || it == ' ' }
                            .take(10)
                        validationError = null
                    },
                    label = "Plate",
                    placeholder = "AB-123-CD",
                    modifier = Modifier.weight(0.6f)
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
            text = if (isEditing) "Save changes" else "Save",
            enabled = !isSaving,
            onClick = {
                if (manufacturer.isBlank() || model.isBlank() || year.isBlank() || plate.isBlank()) {
                    validationError = "All fields are required"
                } else {
                    onSave(manufacturer, model, year, plate)
                }
            }
        )

        if (onDeleteClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f))
            ) {
                Text(text = "Delete vehicle", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDeleteConfirm && onDeleteClick != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete vehicle?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteClick()
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

@Preview(showBackground = true)
@Composable
fun AddVehicleScreenPreview() {
    AddVehicleScreenContent(
        onBackClick = {},
        onSave = { _, _, _, _ -> }
    )
}