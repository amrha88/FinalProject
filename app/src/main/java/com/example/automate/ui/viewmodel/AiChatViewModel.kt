package com.example.automate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.domain.repository.AiChatRepository
import com.example.automate.ui.model.ChatMessageUiModel
import com.example.automate.ui.model.ChatSender
import com.example.automate.ui.model.MessageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AiChatViewModel(private val repository: AiChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    // In-memory storage for different vehicle conversations
    private val conversationStorage = mutableMapOf<String, List<ChatMessageUiModel>>()
    private var currentVehicleId: String? = null

    fun updateInput(text: String) {
        _uiState.update { it.copy(input = text) }
    }

    /**
     * Call this when navigating to the Chatbot screen for a specific vehicle
     */
    fun initializeForVehicle(vehicleId: String) {
        if (currentVehicleId == vehicleId) return
        
        // Save current history if any
        currentVehicleId?.let { oldId ->
            conversationStorage[oldId] = _uiState.value.messages
        }
        
        // Load new history or empty
        currentVehicleId = vehicleId
        _uiState.update { state ->
            state.copy(
                messages = conversationStorage[vehicleId] ?: emptyList(),
                input = "",
                isSending = false,
                errorMessage = null
            )
        }
    }

    fun sendMessage(vehicle: VehicleUiModel) {
        val userText = _uiState.value.input.trim()
        if (userText.isBlank() || _uiState.value.isSending) return

        val userMessage = ChatMessageUiModel(
            id = UUID.randomUUID().toString(),
            text = userText,
            sender = ChatSender.USER,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                input = "",
                isSending = true,
                errorMessage = null
            )
        }

        performChatRequest(vehicle, userText, userMessage.id)
    }

    fun retryLastFailedMessage(vehicle: VehicleUiModel) {
        val lastMessage = _uiState.value.messages.lastOrNull()
        if (lastMessage != null && lastMessage.status == MessageStatus.FAILED && !_uiState.value.isSending) {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            
            // Mark it as sending again
            _uiState.update { state ->
                state.copy(
                    messages = state.messages.map { 
                        if (it.id == lastMessage.id) it.copy(status = MessageStatus.SENDING) else it 
                    }
                )
            }
            
            performChatRequest(vehicle, lastMessage.text, lastMessage.id)
        }
    }

    private fun performChatRequest(vehicle: VehicleUiModel, userText: String, userMessageId: String) {
        viewModelScope.launch {
            val history = _uiState.value.messages
                .filter { it.status == MessageStatus.SENT && it.id != userMessageId }
                .takeLast(20)

            repository.sendMessage(vehicle, history, userText).fold(
                onSuccess = { responseText ->
                    val assistantMessage = ChatMessageUiModel(
                        id = UUID.randomUUID().toString(),
                        text = responseText,
                        sender = ChatSender.ASSISTANT,
                        timestamp = System.currentTimeMillis(),
                        status = MessageStatus.SENT
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { 
                                if (it.id == userMessageId) it.copy(status = MessageStatus.SENT) else it 
                            } + assistantMessage,
                            isSending = false
                        ).also { newState ->
                            // Update storage
                            currentVehicleId?.let { id ->
                                conversationStorage[id] = newState.messages
                            }
                        }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { 
                                if (it.id == userMessageId) it.copy(status = MessageStatus.FAILED) else it 
                            },
                            isSending = false,
                            errorMessage = throwable.message
                        ).also { newState ->
                            // Update storage even on failure (to keep the failed message)
                            currentVehicleId?.let { id ->
                                conversationStorage[id] = newState.messages
                            }
                        }
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
