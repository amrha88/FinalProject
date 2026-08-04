package com.example.automate.ui.viewmodel

import android.net.Uri
import com.example.automate.domain.model.WarningLightResult

sealed class AiAssistantUiState {
    object Initial : AiAssistantUiState()
    data class ImageSelected(val uri: Uri) : AiAssistantUiState()
    data class Analyzing(val uri: Uri) : AiAssistantUiState()
    data class Success(val uri: Uri, val result: WarningLightResult) : AiAssistantUiState()
    data class Error(val message: String) : AiAssistantUiState()
}
