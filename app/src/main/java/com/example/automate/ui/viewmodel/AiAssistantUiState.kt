package com.example.automate.ui.viewmodel

import android.net.Uri
import com.example.automate.domain.model.WarningLightResult

data class AiAssistantUiState(
    val selectedUri: Uri? = null,
    val isAnalyzing: Boolean = false,
    val analysisResult: WarningLightResult? = null,
    val isSavedToHistory: Boolean = false,
    val isSavingToHistory: Boolean = false,
    val saveHistoryError: String? = null,
    val errorMessage: String? = null
) {
    val hasUnsavedResult: Boolean get() = analysisResult != null && !isSavedToHistory
}
