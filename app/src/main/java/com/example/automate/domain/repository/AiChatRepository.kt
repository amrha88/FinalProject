package com.example.automate.domain.repository

import com.example.automate.ui.model.ChatMessageUiModel
import com.example.automate.ui.viewmodel.VehicleUiModel

interface AiChatRepository {
    suspend fun sendMessage(
        vehicle: VehicleUiModel,
        conversationHistory: List<ChatMessageUiModel>,
        userMessage: String
    ): Result<String>
}
