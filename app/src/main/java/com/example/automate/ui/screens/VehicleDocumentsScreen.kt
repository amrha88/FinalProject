package com.example.automate.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.automate.domain.model.VehicleDocument
import com.example.automate.domain.model.VehicleDocumentExtraction
import com.example.automate.domain.model.VehicleDocumentStatus
import com.example.automate.domain.model.VehicleDocumentType
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.VehicleDocumentsUiState
import com.example.automate.ui.viewmodel.VehicleDocumentsViewModel
import com.example.automate.util.FileUtils
import com.example.automate.util.ImageProcessingUtils
import kotlinx.coroutines.launch

@Composable
internal fun documentTypeLabel(type: VehicleDocumentType): String = when (type) {
    VehicleDocumentType.VEHICLE_LICENCE -> stringResource(R.string.doc_type_vehicle_licence)
    VehicleDocumentType.MAINTENANCE -> stringResource(R.string.doc_type_maintenance)
    VehicleDocumentType.INSPECTION -> stringResource(R.string.doc_type_inspection)
    VehicleDocumentType.INSURANCE -> stringResource(R.string.doc_type_insurance)
    VehicleDocumentType.REPAIR_INVOICE -> stringResource(R.string.doc_type_repair_invoice)
    VehicleDocumentType.OTHER -> stringResource(R.string.doc_type_other)
}

@Composable
fun VehicleDocumentsScreen(
    vehicleId: String,
    vehicle: Vehicle?,
    viewModel: VehicleDocumentsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDocumentForView by remember { mutableStateOf<VehicleDocument?>(null) }
    var documentToDelete by remember { mutableStateOf<VehicleDocument?>(null) }

    LaunchedEffect(vehicleId) {
        viewModel.loadDocuments(vehicleId)
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

    val vehicleTitle = listOf(vehicle.manufacturer, vehicle.model, vehicle.year)
        .filter { it.isNotBlank() }
        .joinToString(" ")

    if (selectedDocumentForView != null) {
        DocumentDetailView(
            document = selectedDocumentForView!!,
            vehicleTitle = vehicleTitle,
            onBackClick = { selectedDocumentForView = null },
            onDelete = {
                documentToDelete = selectedDocumentForView
            }
        )
    } else {
        VehicleDocumentsContent(
            vehicleTitle = vehicleTitle,
            vehiclePlate = vehicle.plate,
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
                            viewModel.analyzeDocument(vehicle, bitmap)
                        } else {
                            // Handle error
                        }
                    }
                }
            },
            onSave = { viewModel.saveConfirmedDocument(vehicleId) },
            onCancel = { viewModel.removeSelectedImage() },
            onRemoveImage = { viewModel.removeSelectedImage() },
            onUpdateField = { update -> viewModel.updateExtractedField(update) },
            onDocumentClick = { selectedDocumentForView = it },
            onEditClick = { viewModel.startEditing(it) },
            onReplaceClick = { 
                viewModel.startReplacing(it.id)
                val uri = FileUtils.createImageUri(context)
                tempUri = uri
                cameraLauncher.launch(uri)
            },
            onDeleteClick = { documentToDelete = it },
            onDocumentTypeChange = { viewModel.changeDocumentType(it) }
        )
    }

    if (documentToDelete != null) {
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = { Text(stringResource(R.string.delete_document_dialog_title2)) },
            text = { Text(stringResource(R.string.delete_document_body1)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDocument(vehicleId, documentToDelete!!.id)
                    documentToDelete = null
                    selectedDocumentForView = null
                }) {
                    Text(stringResource(R.string.action_delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDocumentsContent(
    vehicleTitle: String,
    vehiclePlate: String,
    uiState: VehicleDocumentsUiState,
    onBackClick: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onAnalyze: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onRemoveImage: () -> Unit,
    onUpdateField: ((VehicleDocument) -> VehicleDocument) -> Unit,
    onDocumentClick: (VehicleDocument) -> Unit,
    onEditClick: (VehicleDocument) -> Unit,
    onReplaceClick: (VehicleDocument) -> Unit,
    onDeleteClick: (VehicleDocument) -> Unit,
    onDocumentTypeChange: (VehicleDocumentType) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<VehicleDocumentType?>(null) }
    
    val filteredDocuments = if (selectedFilter == null) {
        uiState.documents
    } else {
        uiState.documents.filter { it.documentType == selectedFilter }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.documents_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$vehicleTitle • $vehiclePlate", fontSize = 12.sp, color = Color.LightGray)
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
        containerColor = Color(0xFF000C1F),
        floatingActionButton = {
            if (uiState.selectedImageUri == null && uiState.editableDocument == null) {
                FloatingActionButton(
                    onClick = onTakePhoto,
                    containerColor = Color(0xFF007BFF),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_document))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            if (uiState.editableDocument != null) {
                item {
                    Text(
                        text = stringResource(if (uiState.isEditingExisting) R.string.documents_edit_title else R.string.documents_review_title),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        stringResource(R.string.documents_review_warning),
                        color = Color.Yellow,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DocumentReviewForm(
                        document = uiState.editableDocument,
                        onUpdateField = onUpdateField,
                        onTypeChange = onDocumentTypeChange,
                        uncertainFields = uiState.extraction?.uncertainFields ?: emptyList()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = stringResource(if (uiState.isSaving) R.string.action_saving else R.string.action_save_document),
                        onClick = onSave,
                        enabled = !uiState.isSaving
                    )

                    TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.action_cancel), color = Color.White)
                    }
                }
            } else if (uiState.selectedImageUri != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = stringResource(R.string.cd_selected_document),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.isAnalyzing) {
                        CircularProgressIndicator(color = Color(0xFF007BFF))
                        Text(stringResource(R.string.documents_analyzing), color = Color.White, modifier = Modifier.padding(top = 16.dp))
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
                // Filter Row
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == null,
                                onClick = { selectedFilter = null },
                                label = { Text(stringResource(R.string.document_type_all)) }
                            )
                        }
                        VehicleDocumentType.entries.forEach { type ->
                            item {
                                FilterChip(
                                    selected = selectedFilter == type,
                                    onClick = { selectedFilter = type },
                                    label = { Text(documentTypeLabel(type)) }
                                )
                            }
                        }
                    }
                }

                if (uiState.documents.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.documents_empty_title), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.documents_empty_subtitle),
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            PrimaryButton(text = stringResource(R.string.action_take_photo), onClick = onTakePhoto)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onChooseGallery,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text(stringResource(R.string.action_upload_document))
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.documents_current_title),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    }
                    
                    items(filteredDocuments) { document ->
                        DocumentCard(
                            document = document, 
                            onClick = { onDocumentClick(document) },
                            onEdit = { onEditClick(document) },
                            onReplace = { onReplaceClick(document) },
                            onDelete = { onDeleteClick(document) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun DocumentCard(
    document: VehicleDocument, 
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onReplace: () -> Unit,
    onDelete: () -> Unit
) {
    val typeLabel = documentTypeLabel(document.documentType)
    val date = document.documentDate ?: stringResource(R.string.document_no_date)
    val summary = document.summary ?: stringResource(R.string.document_no_summary)
    var menuExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(document.documentType) {
                        VehicleDocumentType.MAINTENANCE, VehicleDocumentType.REPAIR_INVOICE -> Icons.Default.Build
                        VehicleDocumentType.VEHICLE_LICENCE -> Icons.Default.Description
                        VehicleDocumentType.INSURANCE -> Icons.Default.Security
                        VehicleDocumentType.INSPECTION -> Icons.AutoMirrored.Filled.Assignment
                        else -> Icons.AutoMirrored.Filled.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = Color(0xFF007BFF),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = typeLabel, color = Color(0xFF0B1730), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (document.status == VehicleDocumentStatus.ACTIVE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                stringResource(R.string.document_status_active),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = Color(0xFF2E7D32),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(text = date, color = Color.Gray, fontSize = 12.sp)
                Text(text = summary, color = Color(0xFF6B7280), fontSize = 13.sp, maxLines = 1)
            }
            
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_menu), tint = Color.Gray)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_view_details)) },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                        onClick = { menuExpanded = false; onClick() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_update_replace)) },
                        leadingIcon = { Icon(Icons.Default.Update, contentDescription = null) },
                        onClick = { menuExpanded = false; onReplace() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_edit_information)) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; onEdit() }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete), color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentReviewForm(
    document: VehicleDocument,
    onUpdateField: ((VehicleDocument) -> VehicleDocument) -> Unit,
    onTypeChange: (VehicleDocumentType) -> Unit,
    uncertainFields: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (uncertainFields.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.documents_verify_fields), color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        uncertainFields.joinToString(", "),
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Document Type Selector
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(stringResource(R.string.document_type_prefix, documentTypeLabel(document.documentType)))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                VehicleDocumentType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(documentTypeLabel(type)) },
                        onClick = {
                            onTypeChange(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        AppTextField(
            value = document.documentTitle ?: "",
            onValueChange = { title -> onUpdateField { it.copy(documentTitle = title) } },
            label = stringResource(R.string.label_document_title)
        )

        AppTextField(
            value = document.documentDate ?: "",
            onValueChange = { date -> onUpdateField { it.copy(documentDate = date) } },
            label = stringResource(R.string.label_document_date)
        )

        AppTextField(
            value = document.summary ?: "",
            onValueChange = { sum -> onUpdateField { it.copy(summary = sum) } },
            label = stringResource(R.string.label_short_summary)
        )

        // Specific fields based on type
        when (document.documentType) {
            VehicleDocumentType.MAINTENANCE, VehicleDocumentType.REPAIR_INVOICE -> {
                AppTextField(
                    value = document.mileage?.toString() ?: "",
                    onValueChange = { m -> onUpdateField { it.copy(mileage = m.toIntOrNull()) } },
                    label = stringResource(R.string.label_mileage)
                )
                AppTextField(
                    value = document.garageOrProvider ?: "",
                    onValueChange = { g -> onUpdateField { it.copy(garageOrProvider = g) } },
                    label = stringResource(R.string.label_garage_name)
                )

                BooleanOption(stringResource(R.string.bool_oil_changed), document.oilChanged) { v -> onUpdateField { it.copy(oilChanged = v) } }
                BooleanOption(stringResource(R.string.bool_filter_changed), document.oilFilterChanged) { v -> onUpdateField { it.copy(oilFilterChanged = v) } }
            }
            VehicleDocumentType.INSURANCE -> {
                AppTextField(
                    value = document.insuranceProvider ?: "",
                    onValueChange = { p -> onUpdateField { it.copy(insuranceProvider = p) } },
                    label = stringResource(R.string.label_insurance_provider)
                )
                AppTextField(
                    value = document.insuranceExpiryDate ?: "",
                    onValueChange = { d -> onUpdateField { it.copy(insuranceExpiryDate = d) } },
                    label = stringResource(R.string.label_policy_expiry_date)
                )
            }
            VehicleDocumentType.VEHICLE_LICENCE -> {
                AppTextField(
                    value = document.licenceExpiryDate ?: "",
                    onValueChange = { d -> onUpdateField { it.copy(licenceExpiryDate = d) } },
                    label = stringResource(R.string.label_licence_expiry_date)
                )
            }
            VehicleDocumentType.INSPECTION -> {
                AppTextField(
                    value = document.inspectionExpiryDate ?: "",
                    onValueChange = { d -> onUpdateField { it.copy(inspectionExpiryDate = d) } },
                    label = stringResource(R.string.label_inspection_expiry_date)
                )
            }
            else -> {}
        }
    }
}

@Composable
fun BooleanOption(label: String, value: Boolean?, onValueChange: (Boolean?) -> Unit) {
    Column {
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OptionChip(stringResource(R.string.action_yes), value == true) { onValueChange(true) }
            OptionChip(stringResource(R.string.action_no), value == false) { onValueChange(false) }
            OptionChip(stringResource(R.string.option_unknown), value == null) { onValueChange(null) }
        }
    }
}

@Composable
fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Color(0xFF007BFF) else Color.White.copy(alpha = 0.1f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(label, color = Color.White, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailView(
    document: VehicleDocument,
    vehicleTitle: String,
    onBackClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(documentTypeLabel(document.documentType), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(vehicleTitle, fontSize = 12.sp, color = Color.LightGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = Color.Red)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            val yes = stringResource(R.string.action_yes)
            val no = stringResource(R.string.action_no)
            val unknown = stringResource(R.string.option_unknown)

            DetailRow(stringResource(R.string.detail_title), document.documentTitle)
            DetailRow(stringResource(R.string.detail_date), document.documentDate)
            DetailRow(stringResource(R.string.label_mileage), document.mileage?.toString())
            DetailRow(stringResource(R.string.detail_provider), document.garageOrProvider ?: document.insuranceProvider)
            DetailRow(stringResource(R.string.detail_status), document.status.name)
            DetailRow(stringResource(R.string.detail_summary), document.summary)

            if (document.documentType == VehicleDocumentType.MAINTENANCE) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.maintenance_details_title), fontWeight = FontWeight.Bold, color = Color.White)
                DetailRow(stringResource(R.string.bool_oil_changed), when (document.oilChanged) { true -> yes; false -> no; else -> unknown })
                DetailRow(stringResource(R.string.bool_filter_changed), when (document.oilFilterChanged) { true -> yes; false -> no; else -> unknown })
            }

            if (document.licenceExpiryDate != null) DetailRow(stringResource(R.string.detail_licence_expiry), document.licenceExpiryDate)
            if (document.insuranceExpiryDate != null) DetailRow(stringResource(R.string.detail_insurance_expiry), document.insuranceExpiryDate)
            if (document.inspectionExpiryDate != null) DetailRow(stringResource(R.string.detail_inspection_expiry), document.inspectionExpiryDate)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_document_confirm_title)) },
            text = { Text(stringResource(R.string.delete_document_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(document.id)
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.action_delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VehicleDocumentsEmptyPreview() {
    AutomateTheme {
        VehicleDocumentsContent(
            vehicleTitle = "Volkswagen Polo 2011",
            vehiclePlate = "91-272-30",
            uiState = VehicleDocumentsUiState(),
            onBackClick = {},
            onTakePhoto = {},
            onChooseGallery = {},
            onAnalyze = {},
            onSave = {},
            onCancel = {},
            onRemoveImage = {},
            onUpdateField = {},
            onDocumentClick = {},
            onEditClick = {},
            onReplaceClick = {},
            onDeleteClick = {},
            onDocumentTypeChange = {}
        )
    }
}
