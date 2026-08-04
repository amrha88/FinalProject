package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.ui.model.ChatMessageUiModel
import com.example.automate.ui.model.ChatSender
import com.example.automate.ui.model.MessageStatus
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AiChatUiState
import com.example.automate.ui.viewmodel.AiChatViewModel
import com.example.automate.ui.viewmodel.VehicleUiModel
import kotlinx.coroutines.launch

@Composable
fun ChatbotScreen(
    vehicle: VehicleUiModel?,
    viewModel: AiChatViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehicle?.id) {
        vehicle?.id?.let { viewModel.initializeForVehicle(it) }
    }
    
    if (vehicle == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000C1F)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Vehicle not found", color = Color.White, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackClick) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val vehicleTitle = listOf(vehicle.manufacturer, vehicle.model, vehicle.year)
        .filter { it.isNotBlank() }
        .joinToString(" ")

    ChatbotContent(
        vehicleTitle = vehicleTitle,
        uiState = uiState,
        onInputChange = { viewModel.updateInput(it) },
        onSendClick = { viewModel.sendMessage(vehicle) },
        onRetryClick = { viewModel.retryLastFailedMessage(vehicle) },
        onBackClick = onBackClick,
        onClearError = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotContent(
    vehicleTitle: String,
    uiState: AiChatUiState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onClearError: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("AI Assistant", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
        containerColor = Color(0xFF000C1F),
        bottomBar = {
            ChatInputArea(
                input = uiState.input,
                isSending = uiState.isSending,
                onInputChange = onInputChange,
                onSendClick = onSendClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Ask me about warning lights, maintenance, or your vehicle.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages) { message ->
                        ChatBubble(message = message)
                    }

                    if (uiState.isSending) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                ErrorMessageBar(
                    message = uiState.errorMessage,
                    onRetry = onRetryClick,
                    onDismiss = onClearError
                )
            }
            
            Text(
                text = "AI guidance may be inaccurate. For serious warnings, contact a qualified mechanic.",
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageUiModel) {
    val isUser = message.sender == ChatSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) Color(0xFF007BFF) else Color(0xFF1E1E1E)
    val contentColor = Color.White
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = shape,
            tonalElevation = 2.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
        
        if (message.status == MessageStatus.FAILED) {
            Text(
                "Failed to send",
                color = Color.Red,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Text(
                "AI is typing...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color.Gray,
                fontSize = 13.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
fun ChatInputArea(
    input: String,
    isSending: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1E1E1E),
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (input.isNotBlank() && !isSending) onSendClick() }
                ),
                maxLines = 4
            )

            IconButton(
                onClick = onSendClick,
                enabled = input.isNotBlank() && !isSending
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (input.isNotBlank() && !isSending) Color(0xFF007BFF) else Color.Gray
                )
            }
        }
    }
}

@Composable
fun ErrorMessageBar(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = Color(0xFFB71C1C).copy(alpha = 0.9f),
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, modifier = Modifier.weight(1f), fontSize = 13.sp)
            TextButton(onClick = onRetry) {
                Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatbotScreenPreview() {
    val mockUiState = AiChatUiState(
        messages = listOf(
            ChatMessageUiModel("1", "Hello! How can I help you today?", ChatSender.ASSISTANT, 1000),
            ChatMessageUiModel("2", "What does the check engine light mean?", ChatSender.USER, 2000),
            ChatMessageUiModel("3", "The check engine light indicates that the vehicle's onboard diagnostic system has detected a problem with the engine, transmission, or exhaust system.", ChatSender.ASSISTANT, 3000)
        )
    )
    AutomateTheme {
        ChatbotContent(
            vehicleTitle = "Volkswagen Polo 2011",
            uiState = mockUiState,
            onInputChange = {},
            onSendClick = {},
            onRetryClick = {},
            onBackClick = {},
            onClearError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatbotEmptyPreview() {
    AutomateTheme {
        ChatbotContent(
            vehicleTitle = "Toyota Corolla 2022",
            uiState = AiChatUiState(),
            onInputChange = {},
            onSendClick = {},
            onRetryClick = {},
            onBackClick = {},
            onClearError = {}
        )
    }
}
