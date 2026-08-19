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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.automate.R
import com.example.automate.domain.model.*
import com.example.automate.ui.components.AppDateField
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.VehicleHistoryUiState
import com.example.automate.ui.viewmodel.VehicleHistoryViewModel
import com.example.automate.util.FileUtils
import com.example.automate.util.ImageProcessingUtils
import kotlinx.coroutines.launch

@Composable
internal fun maintenanceItemLabel(type: MaintenanceItemType): String = when (type) {
    MaintenanceItemType.ENGINE_OIL -> stringResource(R.string.maint_engine_oil)
    MaintenanceItemType.OIL_FILTER -> stringResource(R.string.maint_oil_filter)
    MaintenanceItemType.AIR_FILTER -> stringResource(R.string.maint_air_filter)
    MaintenanceItemType.CABIN_FILTER -> stringResource(R.string.maint_cabin_filter)
    MaintenanceItemType.BRAKE_PADS -> stringResource(R.string.maint_brake_pads)
    MaintenanceItemType.BRAKE_DISCS -> stringResource(R.string.maint_brake_discs)
    MaintenanceItemType.BRAKE_FLUID -> stringResource(R.string.maint_brake_fluid)
    MaintenanceItemType.COOLANT -> stringResource(R.string.maint_coolant)
    MaintenanceItemType.SPARK_PLUGS -> stringResource(R.string.maint_spark_plugs)
    MaintenanceItemType.BATTERY -> stringResource(R.string.maint_battery)
    MaintenanceItemType.TIRES -> stringResource(R.string.maint_tires)
    MaintenanceItemType.TIMING_BELT -> stringResource(R.string.maint_timing_belt)
    MaintenanceItemType.TIMING_CHAIN -> stringResource(R.string.maint_timing_chain)
    MaintenanceItemType.TRANSMISSION_OIL -> stringResource(R.string.maint_transmission_oil)
    MaintenanceItemType.FUEL_FILTER -> stringResource(R.string.maint_fuel_filter)
    MaintenanceItemType.OTHER -> stringResource(R.string.doc_type_other)
}

@Composable
internal fun historyEventTypeLabel(type: VehicleHistoryEventType): String = when (type) {
    VehicleHistoryEventType.MAINTENANCE -> stringResource(R.string.doc_type_maintenance)
    VehicleHistoryEventType.REPAIR -> stringResource(R.string.history_event_repair)
    VehicleHistoryEventType.OIL_CHANGE -> stringResource(R.string.history_event_oil_change)
    VehicleHistoryEventType.INSPECTION -> stringResource(R.string.doc_type_inspection)
    VehicleHistoryEventType.LICENCE_RENEWAL -> stringResource(R.string.history_event_licence_renewal)
    VehicleHistoryEventType.INSURANCE_RENEWAL -> stringResource(R.string.history_event_insurance_renewal)
    VehicleHistoryEventType.DOCUMENT_UPDATE -> stringResource(R.string.history_event_document_update)
    VehicleHistoryEventType.WARNING_ANALYSIS -> stringResource(R.string.history_event_warning_analysis)
    VehicleHistoryEventType.NOTIFICATION_SENT -> stringResource(R.string.history_event_notification_sent)
    VehicleHistoryEventType.MANUAL -> stringResource(R.string.history_event_manual)
}

@Composable
internal fun maintenanceActionLabel(action: MaintenanceAction): String = when (action) {
    MaintenanceAction.REPLACED -> stringResource(R.string.maint_action_replaced)
    MaintenanceAction.SERVICED -> stringResource(R.string.maint_action_serviced)
    MaintenanceAction.CHECKED -> stringResource(R.string.maint_action_checked)
}

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
                Text(stringResource(R.string.vehicle_not_found), color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackClick) { Text(stringResource(R.string.action_go_back)) }
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
    val unknownYear = stringResource(R.string.unknown_year)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.history_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HistorySummary(uiState.historyEvents)
                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.editableEvent == null && uiState.selectedImageUri == null && uiState.maintenanceUpdate == null) {
                    Text(
                        text = stringResource(R.string.history_maintenance_status),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    MaintenanceStatusSection(uiState.maintenanceStates, onUpdateComponent)

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PrimaryButton(
                            text = stringResource(R.string.action_add_history),
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
                            Text(stringResource(R.string.action_ai_quick_update), fontSize = 14.sp)
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
                                contentDescription = stringResource(R.string.cd_selected_document),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        if (uiState.isAnalyzing) {
                            CircularProgressIndicator(color = Color(0xFF007BFF))
                            Text(stringResource(R.string.history_analyzing_maintenance), color = Color.White, modifier = Modifier.padding(top = 16.dp))
                        } else {
                            PrimaryButton(text = stringResource(R.string.action_analyze_document), onClick = onAnalyze)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TextButton(onClick = onAddClick, modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.action_replace), color = Color.White)
                                }
                                TextButton(onClick = onRemoveImage, modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.action_remove), color = Color.Red)
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
                        if (uiState.errorMessage != null) {
                            Text(
                                uiState.errorMessage,
                                color = Color(0xFFFF5252),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        } else {
                            Text(stringResource(R.string.history_no_events), color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                }
            } else {
                val groupedEvents = uiState.filteredEvents.groupBy {
                    it.eventDate?.split("-")?.firstOrNull() ?: unknownYear
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
    val name = maintenanceItemLabel(type)

    Card(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, color = Color(0xFF0B1730), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (state?.lastServiceDate != null) {
                Text(text = stringResource(R.string.history_component_last, state.lastServiceDate), color = Color.Gray, fontSize = 11.sp)
                if (state.lastServiceMileage != null) {
                    Text(text = stringResource(R.string.value_km, state.lastServiceMileage.toString()), color = Color.Gray, fontSize = 11.sp)
                }
            } else {
                Text(text = stringResource(R.string.history_component_no_data), color = Color.LightGray, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { onUpdateClick(type) },
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF007BFF).copy(alpha = 0.1f))
            ) {
                Text(stringResource(R.string.action_update), color = Color(0xFF007BFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        Text(stringResource(R.string.history_review_maintenance_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(stringResource(R.string.history_ai_understood), color = Color.Yellow, fontSize = 13.sp)

        if (uncertainFields.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.1f))) {
                Text(
                    text = stringResource(R.string.history_verify_prefix, uncertainFields.joinToString(", ")),
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
                            text = "${maintenanceItemLabel(item.type)} ${maintenanceActionLabel(item.action)}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        AppDateField(value = update.eventDate, onValueChange = { d -> onUpdateField { it.copy(eventDate = d) } }, label = stringResource(R.string.label_date_ymd))
        AppTextField(value = update.mileage?.toString() ?: "", onValueChange = { m -> onUpdateField { it.copy(mileage = m.toIntOrNull()) } }, label = stringResource(R.string.label_mileage))
        AppTextField(value = update.notes ?: "", onValueChange = { n -> onUpdateField { it.copy(notes = n) } }, label = stringResource(R.string.label_notes))

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = stringResource(if (isSaving) R.string.action_saving else R.string.action_confirm_update), onClick = onSave, enabled = !isSaving)
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_cancel), color = Color.White) }
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
                Text(stringResource(R.string.action_ai_quick_update))
            }
        },
        text = {
            Column {
                Text(stringResource(R.string.history_ai_quick_update_hint), fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text(stringResource(R.string.history_ai_quick_update_placeholder), fontSize = 13.sp) },
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
                Text(stringResource(R.string.action_analyze))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
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
            SummaryItem(label = stringResource(R.string.summary_events), value = events.size.toString())
            SummaryItem(label = stringResource(R.string.doc_type_maintenance), value = totalMaintenance.toString())
            if (latestMileage != null) {
                SummaryItem(label = stringResource(R.string.summary_last_mileage), value = stringResource(R.string.value_km, latestMileage.toString()))
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
                label = { Text(stringResource(R.string.document_type_all)) }
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
                    label = { Text(historyEventTypeLabel(filter)) }
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
                    event.mileage?.let { stringResource(R.string.value_km, it.toString()) }
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
        title = { Text(stringResource(R.string.add_history_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.dialog_take_photo))
                }
                Button(onClick = onUploadImage, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.dialog_upload_image))
                }
                OutlinedButton(onClick = onAddManually, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.dialog_add_manually))
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
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
        Text(stringResource(R.string.history_entry_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(stringResource(R.string.history_entry_review_warning), color = Color.Yellow, fontSize = 13.sp)

        if (uncertainFields.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.1f))) {
                Text(
                    text = stringResource(R.string.history_verify_prefix, uncertainFields.joinToString(", ")),
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
                Text(stringResource(R.string.history_event_type_prefix, historyEventTypeLabel(event.type)))
            }
            DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                VehicleHistoryEventType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(historyEventTypeLabel(type)) },
                        onClick = {
                            onUpdateField { it.copy(type = type) }
                            typeMenuExpanded = false
                        }
                    )
                }
            }
        }

        AppTextField(value = event.title, onValueChange = { t -> onUpdateField { it.copy(title = t) } }, label = stringResource(R.string.label_title))
        AppDateField(value = event.eventDate, onValueChange = { d -> onUpdateField { it.copy(eventDate = d) } }, label = stringResource(R.string.label_date_ymd))
        AppTextField(value = event.mileage?.toString() ?: "", onValueChange = { m -> onUpdateField { it.copy(mileage = m.toIntOrNull()) } }, label = stringResource(R.string.label_mileage))
        AppTextField(value = event.garageName ?: "", onValueChange = { g -> onUpdateField { it.copy(garageName = g) } }, label = stringResource(R.string.label_garage))
        AppTextField(value = event.description ?: "", onValueChange = { d -> onUpdateField { it.copy(description = d) } }, label = stringResource(R.string.label_description_notes))
        AppTextField(value = event.totalAmount?.toString() ?: "", onValueChange = { c -> onUpdateField { it.copy(totalAmount = c.toDoubleOrNull()) } }, label = stringResource(R.string.label_cost))

        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.history_next_service_title), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        AppDateField(value = event.nextServiceDate, onValueChange = { d -> onUpdateField { it.copy(nextServiceDate = d) } }, label = stringResource(R.string.label_next_service_date))
        AppTextField(value = event.nextServiceMileage?.toString() ?: "", onValueChange = { m -> onUpdateField { it.copy(nextServiceMileage = m.toIntOrNull()) } }, label = stringResource(R.string.label_next_service_mileage))

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = stringResource(if (isSaving) R.string.action_saving else R.string.action_save_to_history), onClick = onSave, enabled = !isSaving)
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_cancel), color = Color.White) }
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
                        Text(stringResource(R.string.history_event_details_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000C1F), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF000C1F)
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp)) {
            item {
                Text(text = event.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                DetailItem(label = stringResource(R.string.detail_type), value = historyEventTypeLabel(event.type))
                DetailItem(label = stringResource(R.string.detail_date), value = event.eventDate)
                DetailItem(label = stringResource(R.string.label_mileage), value = event.mileage?.let { stringResource(R.string.value_km, it.toString()) })
                DetailItem(label = stringResource(R.string.label_garage), value = event.garageName)
                DetailItem(label = stringResource(R.string.label_cost), value = event.totalAmount?.let { "$it" })
                DetailItem(label = stringResource(R.string.detail_description), value = event.description)

                if (event.nextServiceDate != null || event.nextServiceMileage != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.history_upcoming_service_title), color = Color(0xFF007BFF), fontWeight = FontWeight.Bold)
                    DetailItem(label = stringResource(R.string.label_next_service_date), value = event.nextServiceDate)
                    DetailItem(label = stringResource(R.string.label_next_service_mileage), value = event.nextServiceMileage?.let { stringResource(R.string.value_km, it.toString()) })
                }

                if (event.maintenanceItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.history_maintenance_items_title), color = Color.White, fontWeight = FontWeight.Bold)
                    event.maintenanceItems.forEach { item ->
                        Text("• ${maintenanceItemLabel(item.type)}", color = Color.LightGray)
                    }
                }

                if (event.sourceDocumentId != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.history_linked_document_title), color = Color.Gray, fontSize = 12.sp)
                    Text(stringResource(R.string.history_linked_document_subtitle), color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_history_event_title)) },
            text = { Text(stringResource(R.string.delete_history_event_body)) },
            confirmButton = { TextButton(onClick = { onDelete(event.id) }) { Text(stringResource(R.string.action_delete), color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.action_cancel)) } }
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
