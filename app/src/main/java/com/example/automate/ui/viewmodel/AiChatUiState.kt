package com.example.automate.ui.viewmodel

import com.example.automate.ui.model.ChatMessageUiModel

data class AiChatUiState(
    val messages: List<ChatMessageUiModel> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null
)
