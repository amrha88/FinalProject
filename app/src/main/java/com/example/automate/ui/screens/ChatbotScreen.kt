package com.example.automate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.R
import com.example.automate.ui.components.AiDisclaimerNote
import com.example.automate.ui.components.AutomateRobot
import com.example.automate.ui.components.RobotDisplayMode
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
                Text(stringResource(R.string.vehicle_not_found), color = Color.White, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackClick) {
                    Text(stringResource(R.string.action_go_back))
                }
            }
        }
        return
    }

    val vehicleTitle = listOf(vehicle.manufacturer, vehicle.model)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    
    val vehicleSubTitle = vehicle.year

    ChatbotContent(
        vehicleTitle = vehicleTitle,
        vehicleSubTitle = vehicleSubTitle,
        uiState = uiState,
        onInputChange = { viewModel.updateInput(it) },
        onSendClick = { viewModel.sendMessage(vehicle) },
        onRetryClick = { viewModel.retryLastFailedMessage(vehicle) },
        onBackClick = onBackClick,
        onClearError = { viewModel.clearError() },
        onSuggestionClick = { prompt ->
            viewModel.updateInput(prompt)
            viewModel.sendMessage(vehicle)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotContent(
    vehicleTitle: String,
    vehicleSubTitle: String,
    uiState: AiChatUiState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onClearError: () -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AutomateRobot(mode = RobotDisplayMode.CHAT_AVATAR, size = 30.dp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.chat_ai_assistant_title),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$vehicleTitle • $vehicleSubTitle",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000C1F))
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
                ChatEmptyState(onSuggestionClick = onSuggestionClick)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
            
            AiDisclaimerNote(
                text = stringResource(R.string.chat_ai_disclaimer),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ChatEmptyState(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        stringResource(R.string.chat_suggestion_warning_light),
        stringResource(R.string.chat_suggestion_maintenance),
        stringResource(R.string.chat_suggestion_documents),
        stringResource(R.string.chat_suggestion_history)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AutomateRobot(mode = RobotDisplayMode.CHAT_EMPTY)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.chat_empty_title),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.chat_empty_subtitle),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(
                    text = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessageUiModel) {
    val isUser = message.sender == ChatSender.USER
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp, end = 8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                AutomateRobot(mode = RobotDisplayMode.CHAT_AVATAR, size = 24.dp)
            }
        }
        
        val containerColor = if (isUser) Color(0xFF007BFF) else Color.White.copy(alpha = 0.08f)
        val contentColor = Color.White
        val shape = if (isUser) {
            RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
        } else {
            RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
        }

        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Surface(
                color = containerColor,
                contentColor = contentColor,
                shape = shape
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
                    text = stringResource(R.string.chat_message_failed),
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, end = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 4.dp, end = 8.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            AutomateRobot(mode = RobotDisplayMode.CHAT_AVATAR, size = 24.dp)
        }
        
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_ai_typing),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color.White.copy(alpha = 0.4f),
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
        color = Color(0xFF0B1730), // Darker navy for input area
        modifier = Modifier.fillMaxWidth()
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
                placeholder = { Text(stringResource(R.string.chat_input_placeholder), color = Color.White.copy(alpha = 0.3f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF007BFF)
                ),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (input.isNotBlank() && !isSending) onSendClick() }
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendClick,
                enabled = input.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (input.isNotBlank() && !isSending) Color(0xFF007BFF) else Color.White.copy(alpha = 0.05f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.cd_send),
                    tint = if (input.isNotBlank() && !isSending) Color.White else Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(20.dp)
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
                Text(stringResource(R.string.action_retry), color = Color.White, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_dismiss), modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.7f))
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
            ChatMessageUiModel("3", "The check engine light indicates a potential issue with your engine system.", ChatSender.ASSISTANT, 3000)
        )
    )
    AutomateTheme {
        ChatbotContent(
            vehicleTitle = "Volkswagen Polo",
            vehicleSubTitle = "2011",
            uiState = mockUiState,
            onInputChange = {},
            onSendClick = {},
            onRetryClick = {},
            onBackClick = {},
            onClearError = {},
            onSuggestionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatbotEmptyPreview() {
    AutomateTheme {
        ChatbotContent(
            vehicleTitle = "Toyota Corolla",
            vehicleSubTitle = "2022",
            uiState = AiChatUiState(),
            onInputChange = {},
            onSendClick = {},
            onRetryClick = {},
            onBackClick = {},
            onClearError = {},
            onSuggestionClick = {}
        )
    }
}
