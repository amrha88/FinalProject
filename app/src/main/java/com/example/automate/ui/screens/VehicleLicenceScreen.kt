package com.example.automate.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.automate.R
import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleLicence
import com.example.automate.ui.components.AiDisclaimerNote
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.VehicleLicenceUiState
import com.example.automate.ui.viewmodel.VehicleLicenceViewModel
import com.example.automate.util.FileUtils
import com.example.automate.util.ImageProcessingUtils
import kotlinx.coroutines.launch

@Composable
fun VehicleLicenceScreen(
    vehicleId: String,
    vehicle: Vehicle?,
    viewModel: VehicleLicenceViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(vehicleId) {
        viewModel.loadLicence(vehicleId)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { viewModel.onImageSelected(it.toString()) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it.toString()) }
    }

    if (vehicle == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000C1F)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.vehicle_not_found), color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackClick) { Text(stringResource(R.string.action_go_back)) }
            }
        }
        return
    }

    val vehicleTitle = "${vehicle.manufacturer} ${vehicle.model} ${vehicle.year}"

    VehicleLicenceContent(
        vehicleTitle = vehicleTitle,
        uiState = uiState,
        onBackClick = onBackClick,
        onTakePhoto = {
            val uri = FileUtils.createImageUri(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        },
        onChooseGallery = {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onAnalyze = {
            uiState.selectedImageUri?.let { uriString ->
                scope.launch {
                    val bitmap = ImageProcessingUtils.processImage(context, uriString.toUri())
                    if (bitmap != null) {
                        viewModel.analyzeSelectedImage(vehicle, bitmap)
                    } else {
                        // Handle error
                    }
                }
            }
        },
        onSave = { viewModel.saveLicence(vehicleId) },
        onDelete = { viewModel.deleteLicence(vehicleId) },
        onEdit = { viewModel.startEditing() },
        onCancel = { viewModel.cancelEditing() },
        onRemoveImage = { viewModel.removeSelectedImage() },
        onUpdateField = { field, value ->
            when (field) {
                "ownerName" -> viewModel.updateOwnerName(value)
                "licencePlate" -> viewModel.updateLicencePlate(value)
                "manufacturer" -> viewModel.updateManufacturer(value)
                "model" -> viewModel.updateModel(value)
                "modelCode" -> viewModel.updateModelCode(value)
                "year" -> viewModel.updateYear(value)
                "color" -> viewModel.updateColor(value)
                "chassisNumber" -> viewModel.updateChassisNumber(value)
                "engineNumber" -> viewModel.updateEngineNumber(value)
                "ownershipDate" -> viewModel.updateOwnershipDate(value)
                "registrationDate" -> viewModel.updateRegistrationDate(value)
                "licenceExpiryDate" -> viewModel.updateLicenceExpiryDate(value)
                "inspectionExpiryDate" -> viewModel.updateInspectionExpiryDate(value)
                "fuelType" -> viewModel.updateFuelType(value)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleLicenceContent(
    vehicleTitle: String,
    uiState: VehicleLicenceUiState,
    onBackClick: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onAnalyze: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onRemoveImage: () -> Unit,
    onUpdateField: (String, String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.vehicle_licence_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(vehicleTitle, fontSize = 12.sp, color = Color.LightGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF000C1F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF000C1F)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }

            if (uiState.savedLicence != null && uiState.editableLicence == null && uiState.selectedImageUri == null) {
                // Saved state
                item {
                    SavedLicenceView(
                        licence = uiState.savedLicence,
                        onEdit = onEdit,
                        onReplace = onTakePhoto,
                        onDelete = onDelete
                    )
                }
            } else if (uiState.editableLicence != null) {
                // Editing / Reviewing state
                item {
                    AiDisclaimerNote(
                        text = stringResource(R.string.licence_ai_review_disclaimer),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (uiState.extraction?.uncertainFields?.isNotEmpty() == true) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.1f)),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.licence_uncertain_fields), color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    uiState.extraction.uncertainFields.joinToString(", "),
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    LicenceForm(
                        licence = uiState.editableLicence,
                        onUpdateField = onUpdateField
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = stringResource(if (uiState.isSaving) R.string.action_saving else R.string.action_save),
                        onClick = onSave,
                        enabled = !uiState.isSaving
                    )

                    TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.action_cancel), color = Color.White)
                    }
                }
            } else if (uiState.selectedImageUri != null) {
                // Image selected, not yet analyzed
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = stringResource(R.string.cd_selected_licence),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.isAnalyzing) {
                        CircularProgressIndicator(color = Color(0xFF007BFF))
                        Text(stringResource(R.string.licence_analyzing), color = Color.White, modifier = Modifier.padding(top = 16.dp))
                    } else {
                        PrimaryButton(text = stringResource(R.string.action_analyze_document), onClick = onAnalyze)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = onTakePhoto, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.action_replace), color = Color.White)
                            }
                            TextButton(onClick = onRemoveImage, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.action_remove), color = Color.Red)
                            }
                        }
                    }
                }
            } else {
                // Empty state
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.licence_empty), color = Color.Gray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(32.dp))

                        PrimaryButton(text = stringResource(R.string.action_take_photo), onClick = onTakePhoto)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = onChooseGallery,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(stringResource(R.string.action_upload_from_gallery))
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SavedLicenceView(
    licence: VehicleLicence,
    onEdit: () -> Unit,
    onReplace: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            LicenceDataRow(stringResource(R.string.licence_owner), licence.ownerName)
            LicenceDataRow(stringResource(R.string.label_plate), licence.licencePlate)
            LicenceDataRow(stringResource(R.string.label_manufacturer), licence.manufacturer)
            LicenceDataRow(stringResource(R.string.label_model), licence.model)
            LicenceDataRow(stringResource(R.string.label_year), licence.year)
            LicenceDataRow(stringResource(R.string.licence_chassis_short), licence.chassisNumber)
            LicenceDataRow(stringResource(R.string.licence_expiry), licence.licenceExpiryDate)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.weight(1f).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.action_edit), color = Color.White)
                    }
                }
                IconButton(onClick = onReplace, modifier = Modifier.weight(1f).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.action_replace), color = Color.White)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.weight(1f).background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.action_delete), color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun LicenceDataRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.Gray, fontSize = 14.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LicenceForm(
    licence: VehicleLicence,
    onUpdateField: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTextField(value = licence.ownerName ?: "", onValueChange = { onUpdateField("ownerName", it) }, label = stringResource(R.string.label_owner_name))
        AppTextField(value = licence.licencePlate, onValueChange = { onUpdateField("licencePlate", it) }, label = stringResource(R.string.label_licence_plate))
        AppTextField(value = licence.manufacturer ?: "", onValueChange = { onUpdateField("manufacturer", it) }, label = stringResource(R.string.label_manufacturer))
        AppTextField(value = licence.model ?: "", onValueChange = { onUpdateField("model", it) }, label = stringResource(R.string.label_model))
        AppTextField(value = licence.year ?: "", onValueChange = { onUpdateField("year", it) }, label = stringResource(R.string.label_year))
        AppTextField(value = licence.chassisNumber ?: "", onValueChange = { onUpdateField("chassisNumber", it) }, label = stringResource(R.string.label_chassis_number))
        AppTextField(value = licence.engineNumber ?: "", onValueChange = { onUpdateField("engineNumber", it) }, label = stringResource(R.string.label_engine_number))
        AppTextField(value = licence.licenceExpiryDate ?: "", onValueChange = { onUpdateField("licenceExpiryDate", it) }, label = stringResource(R.string.label_licence_expiry_date))
    }
}

@Preview(showBackground = true)
@Composable
fun VehicleLicenceEmptyPreview() {
    AutomateTheme {
        VehicleLicenceContent(
            vehicleTitle = "Volkswagen Polo 2011",
            uiState = VehicleLicenceUiState(),
            onBackClick = {},
            onTakePhoto = {},
            onChooseGallery = {},
            onAnalyze = {},
            onSave = {},
            onDelete = {},
            onEdit = {},
            onCancel = {},
            onRemoveImage = {},
            onUpdateField = { _, _ -> }
        )
    }
}
