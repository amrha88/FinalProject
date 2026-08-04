package com.example.automate.ui.model

data class ChatMessageUiModel(
    val id: String,
    val text: String,
    val sender: ChatSender,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT
)

enum class ChatSender {
    USER,
    ASSISTANT
}

enum class MessageStatus {
    SENDING,
    SENT,
    FAILED
}
