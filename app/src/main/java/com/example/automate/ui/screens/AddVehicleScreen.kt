package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.viewmodel.AuthViewModel

@Composable
fun AddVehicleScreen(
    viewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    AddVehicleScreenContent(
        onBackClick = onBackClick,
        onSave = { manufacturer, model, year, plate ->
            viewModel.addVehicle(manufacturer, model, year, plate)
            onSaveSuccess()
        }
    )
}

@Composable
fun AddVehicleScreenContent(
    onBackClick: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var manufacturer by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000C1F))
            .padding(24.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Add new vehicle",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = manufacturer,
            onValueChange = { manufacturer = it; error = null },
            label = "Manufacturer"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = model,
            onValueChange = { model = it; error = null },
            label = "Model"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = year,
            onValueChange = { year = it; error = null },
            label = "Year"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = plate,
            onValueChange = { plate = it; error = null },
            label = "License Plate"
        )

        Spacer(modifier = Modifier.height(24.dp))

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Save",
            onClick = {
                if (manufacturer.isBlank() || model.isBlank() || year.isBlank() || plate.isBlank()) {
                    error = "All fields are required"
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
