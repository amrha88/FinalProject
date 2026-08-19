package com.example.automate.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.automate.R
import com.example.automate.domain.model.WarningLightResult
import com.example.automate.domain.model.WarningSeverity
import com.example.automate.ui.components.*
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AiAssistantUiState
import com.example.automate.ui.viewmodel.AiAssistantViewModel
import com.example.automate.util.FileUtils
import com.example.automate.util.ImageProcessingUtils
import kotlinx.coroutines.launch

@Composable
fun AiAssistantScreen(
    vehicleId: String,
    viewModel: AiAssistantViewModel,
    onNavigateToChat: (WarningLightResult) -> Unit,
    onOpenChatClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingContinuation by remember { mutableStateOf<() -> Unit>({}) }

    val handleBack: () -> Unit = {
        if (uiState.hasUnsavedResult) {
            pendingContinuation = { onBackClick?.invoke() }
            showSaveDialog = true
        } else {
            viewModel.resetState()
            onBackClick?.invoke()
        }
    }

    val handleNewImage: () -> Unit = {
        if (uiState.hasUnsavedResult) {
            pendingContinuation = {}
            showSaveDialog = true
        } else {
            viewModel.resetState()
        }
    }

    BackHandler(enabled = true, onBack = handleBack)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { viewModel.onImageSelected(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = FileUtils.createImageUri(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        }
    }

    AiAssistantContent(
        uiState = uiState,
        onTakePhoto = {
            val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                val uri = FileUtils.createImageUri(context)
                tempUri = uri
                cameraLauncher.launch(uri)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onChooseGallery = { galleryLauncher.launch("image/*") },
        onRetake = { viewModel.onRemoveImage() },
        onRemove = { viewModel.onRemoveImage() },
        onAnalyze = {
            val selectedUri = uiState.selectedUri
            if (selectedUri != null) {
                scope.launch {
                    val bitmap = ImageProcessingUtils.processImage(context, selectedUri)
                    if (bitmap != null) {
                        viewModel.startAnalysis(bitmap)
                    }
                }
            }
        },
        onAskAi = { result -> onNavigateToChat(result) },
        onOpenChatClick = onOpenChatClick,
        onBackClick = handleBack,
        onNewImage = handleNewImage,
        bottomBar = bottomBar
    )

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isSavingToHistory) {
                    showSaveDialog = false
                    viewModel.clearSaveHistoryError()
                }
            },
            title = { Text(stringResource(R.string.save_to_history_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.save_to_history_dialog_body))
                    if (uiState.saveHistoryError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.saveHistoryError!!, color = Color.Red, fontSize = 13.sp)
                    }
                    if (uiState.isSavingToHistory) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(color = Color(0xFF007BFF), modifier = Modifier.size(20.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isSavingToHistory,
                    onClick = {
                        viewModel.saveToHistory(vehicleId) {
                            showSaveDialog = false
                            viewModel.resetState()
                            pendingContinuation()
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        enabled = !uiState.isSavingToHistory,
                        onClick = {
                            showSaveDialog = false
                            viewModel.resetState()
                            pendingContinuation()
                        }
                    ) {
                        Text(stringResource(R.string.action_dont_save))
                    }
                    TextButton(
                        enabled = !uiState.isSavingToHistory,
                        onClick = {
                            showSaveDialog = false
                            viewModel.clearSaveHistoryError()
                        }
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantContent(
    uiState: AiAssistantUiState,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onRetake: () -> Unit,
    onRemove: () -> Unit,
    onAnalyze: () -> Unit,
    onAskAi: (WarningLightResult) -> Unit,
    onOpenChatClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    onNewImage: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    Scaffold(
        bottomBar = bottomBar,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AutomateRobot(mode = RobotDisplayMode.CHAT_AVATAR, size = 26.dp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(stringResource(R.string.ai_car_assistant_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
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
        if (uiState.selectedUri == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AiAssistantHeader(onOpenChatClick = onOpenChatClick)

                Spacer(modifier = Modifier.weight(1f))

                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    OutlinedButton(
                        onClick = onTakePhoto,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_take_photo))
                    }
                    OutlinedButton(
                        onClick = onChooseGallery,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_choose_from_gallery))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AsyncImage(
                        model = uiState.selectedUri,
                        contentDescription = stringResource(R.string.cd_selected_warning_light),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                if (uiState.analysisResult != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onNewImage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(stringResource(R.string.action_new_image))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.analysisResult == null && !uiState.isAnalyzing && uiState.errorMessage == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.action_retake), color = Color.White)
                        }
                        TextButton(onClick = onRemove, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.action_remove), color = Color.Red)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(text = stringResource(R.string.action_analyze_warning_light), onClick = onAnalyze)
                }

                if (uiState.isAnalyzing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(color = Color(0xFF007BFF))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.ai_analyzing_warning_light), color = Color.White)
                    }
                }

                if (uiState.analysisResult != null) {
                    ResultCard(result = uiState.analysisResult, onAskAi = onAskAi)
                }

                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage, color = Color.Red, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(text = stringResource(R.string.action_try_again), onClick = onRemove)
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun AiAssistantHeader(onOpenChatClick: () -> Unit) {
    Text(
        text = stringResource(R.string.ai_scan_title),
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.ai_scan_subtitle),
        color = Color.LightGray,
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB71C1C))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.ai_no_camera_while_driving),
                color = Color(0xFFE57373),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
    Spacer(modifier = Modifier.height(28.dp))
    AssistantBanner(
        title = stringResource(R.string.ai_chat_banner_title),
        subtitle = stringResource(R.string.ai_chat_banner_subtitle),
        onClick = onOpenChatClick
    )
}

@Composable
fun ResultCard(result: WarningLightResult, onAskAi: (WarningLightResult) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.warningName,
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                SeverityBadge(severity = result.severity)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.result_confidence, (result.confidence * 100).toInt()),
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.result_explanation), fontWeight = FontWeight.Bold, color = Color.Black)
            Text(result.explanation, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.result_recommendation), fontWeight = FontWeight.Bold, color = Color.Black)
            Text(result.recommendation, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))

            val canDriveText = when (result.canContinueDriving) {
                true -> stringResource(R.string.result_can_drive_yes)
                false -> stringResource(R.string.result_can_drive_no)
                else -> stringResource(R.string.result_can_drive_unknown)
            }
            val driveColor = if (result.canContinueDriving == true) Color(0xFF2E7D32) else Color(0xFFC62828)

            Text(
                text = canDriveText,
                color = driveColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            AiDisclaimerNote(text = result.disclaimer)
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(text = stringResource(R.string.action_ask_ai_about_warning), onClick = { onAskAi(result) })
        }
    }
}

@Composable
fun SeverityBadge(severity: WarningSeverity) {
    val (backgroundColor, textColor) = when (severity) {
        WarningSeverity.LOW -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        WarningSeverity.MEDIUM -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        WarningSeverity.HIGH -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        WarningSeverity.CRITICAL -> Color(0xFFB71C1C) to Color.White
        WarningSeverity.UNKNOWN -> Color.LightGray to Color.DarkGray
    }
    val label = when (severity) {
        WarningSeverity.LOW -> stringResource(R.string.severity_low)
        WarningSeverity.MEDIUM -> stringResource(R.string.severity_medium)
        WarningSeverity.HIGH -> stringResource(R.string.severity_high)
        WarningSeverity.CRITICAL -> stringResource(R.string.severity_critical)
        WarningSeverity.UNKNOWN -> stringResource(R.string.severity_unknown)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AiAssistantPreview() {
    AutomateTheme {
        AiAssistantContent(
            uiState = AiAssistantUiState(),
            onTakePhoto = {},
            onChooseGallery = {},
            onRetake = {},
            onRemove = {},
            onAnalyze = {},
            onAskAi = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AiAssistantResultPreview() {
    val mockResult = WarningLightResult(
        id = "1",
        warningName = "Check Engine",
        confidence = 0.95f,
        severity = WarningSeverity.HIGH,
        explanation = "The engine control unit has detected a malfunction in the engine system or emission control.",
        recommendation = "Schedule an appointment with a mechanic as soon as possible for a diagnostic scan.",
        canContinueDriving = true,
        disclaimer = "This is an AI estimate. Please consult a professional mechanic."
    )
    AutomateTheme {
        AiAssistantContent(
            uiState = AiAssistantUiState(selectedUri = Uri.EMPTY, analysisResult = mockResult),
            onTakePhoto = {},
            onChooseGallery = {},
            onRetake = {},
            onRemove = {},
            onAnalyze = {},
            onAskAi = {}
        )
    }
}
