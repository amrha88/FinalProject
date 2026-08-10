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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.automate.domain.model.*
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.VehicleHistoryUiState
import com.example.automate.ui.viewmodel.VehicleHistoryViewModel
import com.example.automate.util.FileUtils
import com.example.automate.util.ImageProcessingUtils
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun VehicleHistoryScreen(
    vehicleId: String,
    vehicle: Vehicle?,
    viewModel: VehicleHistoryViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var selectedEventForView by remember { mutableStateOf<VehicleHistoryEvent?>(null) }
    var showAddOptions by remember { mutableStateOf(false) }
    var showAiQuickUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(vehicleId) {
        viewModel.loadHistory(vehicleId)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { viewModel.onImageSelected(it.toString()) }
            showAddOptions = false
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it.toString()) }
        showAddOptions = false
    }

    if (vehicle == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000C1F)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Vehicle not found", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackClick) { Text("Go Back") }
            }
        }
        return
    }

    val vehicleTitle = "${vehicle.manufacturer} ${vehicle.model}"

    if (selectedEventForView != null) {
        HistoryEventDetailView(
            event = selectedEventForView!!,
            vehicleTitle = vehicleTitle,
            onBackClick = { selectedEventForView = null },
            onDelete = {
                viewModel.deleteHistoryEvent(vehicleId, it)
                selectedEventForView = null
            }
        )
    } else {
        VehicleHistoryContent(
            vehicleTitle = vehicleTitle,
            uiState = uiState,
            onBackClick = onBackClick,
            onAddClick = { showAddOptions = true },
            onAiQuickUpdateClick = { showAiQuickUpdate = true },
            onFilterChange = { viewModel.setFilter(it) },
            onAnalyze = {
                uiState.selectedImageUri?.let { uriString ->
                    scope.launch {
                        val bitmap = ImageProcessingUtils.processImage(context, uriString.toUri())
                        if (bitmap != null) {
                            viewModel.analyzeMaintenanceImage(vehicle, bitmap)
                        }
                    }
                }
            },
            onSave = { viewModel.saveHistoryEvent(vehicleId) },
            onSaveMaintenance = { viewModel.saveMaintenanceUpdate(vehicleId) },
            onCancel = { 
                viewModel.cancelEntry() 
                showAiQuickUpdate = false
            },
            onRemoveImage = { viewModel.removeSelectedImage() },
            onUpdateField = { update -> viewModel.updateEditableEvent(update) },
            onUpdateMaintenanceField = { update -> viewModel.updateMaintenanceUpdate(update) },
            onEventClick = { selectedEventForView = it },
            onUpdateComponent = { component -> 
                // Prefill with current mileage if we can find it in the latest history
                val lastMileage = uiState.historyEvents.mapNotNull { it.mileage }.maxOrNull()
                viewModel.startComponentUpdate(vehicleId, component, lastMileage)
            }
        )
    }

    if (showAddOptions) {
        AddHistoryOptionsDialog(
            onDismiss = { showAddOptions = false },
            onTakePhoto = {
                val uri = FileUtils.createImageUri(context)
                tempUri = uri
                cameraLauncher.launch(uri)
            },
            onUploadImage = {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onAddManually = {
                viewModel.startManualEntry(vehicleId)
                showAddOptions = false
            }
        )
    }

    if (showAiQuickUpdate && uiState.maintenanceUpdate == null && !uiState.isAnalyzing) {
        AiQuickUpdateDialog(
            onDismiss = { showAiQuickUpdate = false },
            onAnalyze = { text -> viewModel.analyzeQuickUpdateText(text) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleHistoryContent(
    vehicleTitle: String,
    uiState: VehicleHistoryUiState,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onAiQuickUpdateClick: () -> Unit,
    onFilterChange: (VehicleHistoryEventType?) -> Unit,
    onAnalyze: () -> Unit,
    onSave: () -> Unit,
    onSaveMaintenance: () -> Unit,
    onCancel: () -> Unit,
    onRemoveImage: () -> Unit,
    onUpdateField: ((VehicleHistoryEvent) -> VehicleHistoryEvent) -> Unit,
    onUpdateMaintenanceField: ((ConfirmedMaintenanceUpdate) -> ConfirmedMaintenanceUpdate) -> Unit,
    onEventClick: (VehicleHistoryEvent) -> Unit,
    onUpdateComponent: (MaintenanceItemType) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Vehicle History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(vehicleTitle, fontSize = 12.sp, color = Color.LightGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HistorySummary(uiState.historyEvents)
                Spacer(modifier = Modifier.height(24.dp))
                
                if (uiState.editableEvent == null && uiState.selectedImageUri == null && uiState.maintenanceUpdate == null) {
                    Text(
                        text = "Maintenance status",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    
                    MaintenanceStatusSection(uiState.maintenanceStates, onUpdateComponent)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PrimaryButton(
                            text = "+ Add history", 
                            onClick = onAddClick,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onAiQuickUpdateClick,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Quick Update", fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HistoryFilters(uiState.selectedFilter, onFilterChange)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (uiState.maintenanceUpdate != null) {
                item {
                    MaintenanceUpdateReviewForm(
                        update = uiState.maintenanceUpdate,
                        onUpdateField = onUpdateMaintenanceField,
                        uncertainFields = uiState.textExtraction?.uncertainFields ?: emptyList(),
                        isSaving = uiState.isSaving,
                        onSave = onSaveMaintenance,
                        onCancel = onCancel
                    )
                }
            } else if (uiState.editableEvent != null) {
                item {
                    HistoryEventEntryForm(
                        event = uiState.editableEvent,
                        onUpdateField = onUpdateField,
                        uncertainFields = uiState.extraction?.uncertainFields ?: emptyList(),
                        isSaving = uiState.isSaving,
                        onSave = onSave,
                        onCancel = onCancel
                    )
                }
            } else if (uiState.selectedImageUri != null) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            AsyncImage(
                                model = uiState.selectedImageUri,
                                contentDescription = "Selected document",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        if (uiState.isAnalyzing) {
                            CircularProgressIndicator(color = Color(0xFF007BFF))
                            Text("Analyzing maintenance document...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                        } else {
                            PrimaryButton(text = "Analyze document", onClick = onAnalyze)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TextButton(onClick = onAddClick, modifier = Modifier.weight(1f)) {
                                    Text("Replace", color = Color.White)
                                }
                                TextButton(onClick = onRemoveImage, modifier = Modifier.weight(1f)) {
                                    Text("Remove", color = Color.Red)
                                }
                            }
                        }
                    }
                }
            } else if (uiState.filteredEvents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No history events yet", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                val groupedEvents = uiState.filteredEvents.groupBy { 
                    it.eventDate?.split("-")?.firstOrNull() ?: "Unknown Year"
                }
                
                groupedEvents.forEach { (year, events) ->
                    item {
                        Text(
                            text = year,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                    items(events) { event ->
                        HistoryEventCard(event = event, onClick = { onEventClick(event) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun MaintenanceStatusSection(
    states: List<VehicleMaintenanceItemState>,
    onUpdateComponent: (MaintenanceItemType) -> Unit
) {
    val importantComponents = listOf(
        MaintenanceItemType.ENGINE_OIL,
        MaintenanceItemType.OIL_FILTER,
        MaintenanceItemType.AIR_FILTER,
        MaintenanceItemType.BRAKE_PADS,
        MaintenanceItemType.BATTERY,
        MaintenanceItemType.TIRES
    )

    LazyRow(
        contentPadding = PaddingValues(end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(importantComponents) { type ->
            val state = states.find { it.type == type }
            ComponentStatusCard(type, state, onUpdateComponent)
        }
    }
}

@Composable
fun ComponentStatusCard(
    type: MaintenanceItemType,
    state: VehicleMaintenanceItemState?,
    onUpdateClick: (MaintenanceItemType) -> Unit
) {
    val name = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    
    Card(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, color = Color(0xFF0B1730), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (state?.lastServiceDate != null) {
                Text(text = "Last: ${state.lastServiceDate}", color = Color.Gray, fontSize = 11.sp)
                if (state.lastServiceMileage != null) {
                    Text(text = "${state.lastServiceMileage} km", color = Color.Gray, fontSize = 11.sp)
                }
            } else {
                Text(text = "No data yet", color = Color.LightGray, fontSize = 11.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = { onUpdateClick(type) },
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF007BFF).copy(alpha = 0.1f))
            ) {
                Text("Update", color = Color(0xFF007BFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MaintenanceUpdateReviewForm(
    update: ConfirmedMaintenanceUpdate,
    onUpdateField: ((ConfirmedMaintenanceUpdate) -> ConfirmedMaintenanceUpdate) -> Unit,
    uncertainFields: List<String>,
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Review Maintenance Update", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("AI understood these items from your text:", color = Color.Yellow, fontSize = 13.sp)

        if (uncertainFields.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.1f))) {
                Text(
                    text = "Verify: ${uncertainFields.joinToString(", ")}",
                    modifier = Modifier.padding(12.dp),
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        // Display detected items
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            update.items.forEach { item ->
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.type.name.replace("_", " ").lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} ${item.action.name.lowercase(Locale.ROOT)}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        AppTextField(value = update.eventDate, onValueChange = { d -> onUpdateField { it.copy(eventDate = d) } }, label = "Date (YYYY-MM-DD)")
        AppTextField(value = update.mileage?.toString() ?: "", onValueChange = { m -> onUpdateField { it.copy(mileage = m.toIntOrNull()) } }, label = "Mileage")
        AppTextField(value = update.notes ?: "", onValueChange = { n -> onUpdateField { it.copy(notes = n) } }, label = "Notes")
        
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = if (isSaving) "Saving..." else "Confirm update", onClick = onSave, enabled = !isSaving)
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = Color.White) }
    }
}

@Composable
fun AiQuickUpdateDialog(
    onDismiss: () -> Unit,
    onAnalyze: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF673AB7))
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Quick Update")
            }
        },
        text = {
            Column {
                Text("Tell us what you replaced or serviced", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("e.g. Today I changed the engine oil and filter at 92,400 km.", fontSize = 13.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAnalyze(text) },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
            ) {
                Text("Analyze")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun HistorySummary(events: List<VehicleHistoryEvent>) {
    val totalMaintenance = events.count { it.type == VehicleHistoryEventType.MAINTENANCE || it.type == VehicleHistoryEventType.OIL_CHANGE }
    val latestMileage = events.mapNotNull { it.mileage }.maxOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(label = "Events", value = events.size.toString())
            SummaryItem(label = "Maintenance", value = totalMaintenance.toString())
            if (latestMileage != null) {
                SummaryItem(label = "Last Mileage", value = "${latestMileage} km")
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HistoryFilters(selected: VehicleHistoryEventType?, onFilterChange: (VehicleHistoryEventType?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onFilterChange(null) },
                label = { Text("All") }
            )
        }
        val filters = listOf(
            VehicleHistoryEventType.MAINTENANCE,
            VehicleHistoryEventType.REPAIR,
            VehicleHistoryEventType.INSPECTION,
            VehicleHistoryEventType.NOTIFICATION_SENT
        )
        filters.forEach { filter ->
            item {
                FilterChip(
                    selected = selected == filter,
                    onClick = { onFilterChange(filter) },
                    label = { 
                        Text(filter.name.lowercase(Locale.ROOT)
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }) 
                    }
                )
            }
        }
    }
}

@Composable
fun HistoryEventCard(event: VehicleHistoryEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(event.type) {
                        VehicleHistoryEventType.MAINTENANCE -> Icons.Default.Build
                        VehicleHistoryEventType.REPAIR -> Icons.Default.Handyman
                        VehicleHistoryEventType.OIL_CHANGE -> Icons.Default.WaterDrop
                        VehicleHistoryEventType.NOTIFICATION_SENT -> Icons.Default.Notifications
                        else -> Icons.Default.Event
                    },
                    contentDescription = null,
                    tint = Color(0xFF007BFF),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, color = Color(0xFF0B1730), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val subtitle = listOfNotNull(
                    event.eventDate,
                    event.mileage?.let { "$it km" }
                ).joinToString(" • ")
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun AddHistoryOptionsDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onUploadImage: () -> Unit,
    onAddManually: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Vehicle History") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo")
                }
                Button(onClick = onUploadImage, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Image")
                }
                OutlinedButton(onClick = onAddManually, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Manually")
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun HistoryEventEntryForm(
    event: VehicleHistoryEvent,
    onUpdateField: ((VehicleHistoryEvent) -> VehicleHistoryEvent) -> Unit,
    uncertainFields: List<String>,
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var typeMenuExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Enter History Information", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Review this information before saving. AI extraction may contain errors.", color = Color.Yellow, fontSize = 13.sp)

        if (uncertainFields.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.1f))) {
                Text(
                    text = "Verify: ${uncertainFields.joinToString(", ")}",
                    modifier = Modifier.padding(12.dp),
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }

        // Type Selector
        Box {
            OutlinedButton(
                onClick = { typeMenuExpanded = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Event Type: ${event.type.name.lowercase().replaceFirstChar { it.uppercase() }}")
            }
            DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                VehicleHistoryEventType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onUpdateField { it.copy(type = type) }
                            typeMenuExpanded = false
                        }
                    )
                }
            }
        }

        AppTextField(value = event.title, onValueChange = { t -> onUpdateField { it.copy(title = t) } }, label = "Title")
        AppTextField(value = event.eventDate ?: "", onValueChange = { d -> onUpdateField { it.copy(eventDate = d) } }, label = "Date (YYYY-MM-DD)")
        AppTextField(value = event.mileage?.toString() ?: "", onValueChange = { m -> onUpdateField { it.copy(mileage = m.toIntOrNull()) } }, label = "Mileage")
        AppTextField(value = event.garageName ?: "", onValueChange = { g -> onUpdateField { it.copy(garageName = g) } }, label = "Garage")
        AppTextField(value = event.description ?: "", onValueChange = { d -> onUpdateField { it.copy(description = d) } }, label = "Description/Notes")
        AppTextField(value = event.totalAmount?.toString() ?: "", onValueChange = { c -> onUpdateField { it.copy(totalAmount = c.toDoubleOrNull()) } }, label = "Cost")
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("Next Service Recommendation", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        AppTextField(value = event.nextServiceDate ?: "", onValueChange = { d -> onUpdateField { it.copy(nextServiceDate = d) } }, label = "Next Service Date")
        AppTextField(value = event.nextServiceMileage?.toString() ?: "", onValueChange = { m -> onUpdateField { it.copy(nextServiceMileage = m.toIntOrNull()) } }, label = "Next Service Mileage")

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = if (isSaving) "Saving..." else "Save to History", onClick = onSave, enabled = !isSaving)
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = Color.White) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryEventDetailView(
    event: VehicleHistoryEvent,
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
                        Text("Event Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(vehicleTitle, fontSize = 12.sp, color = Color.LightGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000C1F), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF000C1F)
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp)) {
            item {
                Text(text = event.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                DetailItem(label = "Type", value = event.type.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase(Locale.ROOT) })
                DetailItem(label = "Date", value = event.eventDate)
                DetailItem(label = "Mileage", value = event.mileage?.let { "$it km" })
                DetailItem(label = "Garage", value = event.garageName)
                DetailItem(label = "Cost", value = event.totalAmount?.let { "$it" })
                DetailItem(label = "Description", value = event.description)
                
                if (event.nextServiceDate != null || event.nextServiceMileage != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Upcoming Service", color = Color(0xFF007BFF), fontWeight = FontWeight.Bold)
                    DetailItem(label = "Next Service Date", value = event.nextServiceDate)
                    DetailItem(label = "Next Service Mileage", value = event.nextServiceMileage?.let { "$it km" })
                }

                if (event.maintenanceItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Maintenance Items", color = Color.White, fontWeight = FontWeight.Bold)
                    event.maintenanceItems.forEach { item ->
                        Text("• ${item.type.name.replace("_", " ").lowercase(Locale.ROOT).replaceFirstChar { it.uppercase(Locale.ROOT) }}", color = Color.LightGray)
                    }
                }
                
                if (event.sourceDocumentId != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Linked Document", color = Color.Gray, fontSize = 12.sp)
                    Text("Analyzed from uploaded image", color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete history event?") },
            text = { Text("This will permanently remove this record from your car's history.") },
            confirmButton = { TextButton(onClick = { onDelete(event.id) }) { Text("Delete", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun DetailItem(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VehicleHistoryEmptyPreview() {
    AutomateTheme {
        VehicleHistoryContent(
            vehicleTitle = "Volkswagen Polo",
            uiState = VehicleHistoryUiState(),
            onBackClick = {},
            onAddClick = {},
            onAiQuickUpdateClick = {},
            onFilterChange = {},
            onAnalyze = {},
            onSave = {},
            onSaveMaintenance = {},
            onCancel = {},
            onRemoveImage = {},
            onUpdateField = {},
            onUpdateMaintenanceField = {},
            onEventClick = {},
            onUpdateComponent = {}
        )
    }
}
