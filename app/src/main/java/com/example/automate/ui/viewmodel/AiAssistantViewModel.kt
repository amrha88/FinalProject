package com.example.automate.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.domain.repository.WarningLightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiAssistantViewModel(private val repository: WarningLightRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AiAssistantUiState>(AiAssistantUiState.Initial)
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _uiState.update { AiAssistantUiState.ImageSelected(uri) }
    }

    fun onRemoveImage() {
        _uiState.update { AiAssistantUiState.Initial }
    }

    fun startAnalysis(bitmap: Bitmap) {
        val currentState = _uiState.value
        if (currentState is AiAssistantUiState.ImageSelected || currentState is AiAssistantUiState.Success) {
            val uri = when (currentState) {
                is AiAssistantUiState.ImageSelected -> currentState.uri
                is AiAssistantUiState.Success -> currentState.uri
                else -> return
            }

            _uiState.update { AiAssistantUiState.Analyzing(uri) }

            viewModelScope.launch {
                repository.analyzeWarningLight(bitmap).fold(
                    onSuccess = { result ->
                        _uiState.update { AiAssistantUiState.Success(uri, result) }
                    },
                    onFailure = { throwable ->
                        _uiState.update { AiAssistantUiState.Error(throwable.message ?: "Unknown error occurred") }
                    }
                )
            }
        }
    }

    fun resetState() {
        _uiState.update { AiAssistantUiState.Initial }
    }
}
