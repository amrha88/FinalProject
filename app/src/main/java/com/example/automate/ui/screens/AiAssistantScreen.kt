package com.example.automate.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.automate.domain.model.WarningLightResult
import com.example.automate.domain.model.WarningSeverity
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AiAssistantUiState
import com.example.automate.ui.viewmodel.AiAssistantViewModel
import com.example.automate.util.FileUtils

@Composable
fun AiAssistantScreen(
    viewModel: AiAssistantViewModel,
    onBackClick: () -> Unit,
    onNavigateToChat: (WarningLightResult) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }

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
        onBackClick = onBackClick,
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
        onAnalyze = { viewModel.startAnalysis() },
        onAskAi = { result -> onNavigateToChat(result) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantContent(
    uiState: AiAssistantUiState,
    onBackClick: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onRetake: () -> Unit,
    onRemove: () -> Unit,
    onAnalyze: () -> Unit,
    onAskAi: (WarningLightResult) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Car Assistant", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Scan a dashboard warning light",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Park safely, turn off the vehicle if necessary, and photograph the illuminated warning symbol.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                            text = "Do not use the camera while driving.",
                            color = Color(0xFFE57373),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            when (uiState) {
                is AiAssistantUiState.Initial -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = onTakePhoto,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Take photo")
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
                                Text("Choose from gallery")
                            }
                        }
                    }
                }
                is AiAssistantUiState.ImageSelected, 
                is AiAssistantUiState.Analyzing, 
                is AiAssistantUiState.Success,
                is AiAssistantUiState.Error -> {
                    val uri = when (uiState) {
                        is AiAssistantUiState.ImageSelected -> uiState.uri
                        is AiAssistantUiState.Analyzing -> uiState.uri
                        is AiAssistantUiState.Success -> uiState.uri
                        else -> null
                    }

                    item {
                        if (uri != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected warning light",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        if (uiState is AiAssistantUiState.ImageSelected) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TextButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                                    Text("Retake", color = Color.White)
                                }
                                TextButton(onClick = onRemove, modifier = Modifier.weight(1f)) {
                                    Text("Remove", color = Color.Red)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(text = "Analyze warning light", onClick = onAnalyze)
                        }

                        if (uiState is AiAssistantUiState.Analyzing) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(color = Color(0xFF007BFF))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Analyzing warning light...", color = Color.White)
                            }
                        }

                        if (uiState is AiAssistantUiState.Success) {
                            ResultCard(result = uiState.result, onAskAi = onAskAi)
                        }

                        if (uiState is AiAssistantUiState.Error) {
                            Text(uiState.message, color = Color.Red, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(text = "Try Again", onClick = onRemove)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
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
                text = "Confidence: ${(result.confidence * 100).toInt()}%",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Explanation", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(result.explanation, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recommendation", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(result.recommendation, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            
            val canDriveText = when (result.canContinueDriving) {
                true -> "You may continue driving with caution."
                false -> "Stop driving immediately and seek assistance."
                else -> "Status unknown. Exercise extreme caution."
            }
            val driveColor = if (result.canContinueDriving == true) Color(0xFF2E7D32) else Color(0xFFC62828)
            
            Text(
                text = canDriveText,
                color = driveColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = result.disclaimer,
                color = Color.Gray,
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(text = "Ask the AI about this warning", onClick = { onAskAi(result) })
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
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = severity.name,
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
            uiState = AiAssistantUiState.Initial,
            onBackClick = {},
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
            uiState = AiAssistantUiState.Success(Uri.EMPTY, mockResult),
            onBackClick = {},
            onTakePhoto = {},
            onChooseGallery = {},
            onRetake = {},
            onRemove = {},
            onAnalyze = {},
            onAskAi = {}
        )
    }
}
