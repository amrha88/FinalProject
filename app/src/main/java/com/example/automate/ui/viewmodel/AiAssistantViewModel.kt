package com.example.automate.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.domain.model.VehicleHistoryEvent
import com.example.automate.domain.model.VehicleHistoryEventType
import com.example.automate.domain.repository.VehicleHistoryRepository
import com.example.automate.domain.repository.WarningLightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AiAssistantViewModel(
    private val repository: WarningLightRepository,
    private val historyRepository: VehicleHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _uiState.update { 
            AiAssistantUiState(
                selectedUri = uri,
                isAnalyzing = false,
                analysisResult = null,
                isSavedToHistory = false,
                errorMessage = null
            )
        }
    }

    fun onRemoveImage() {
        _uiState.update { AiAssistantUiState() }
    }

    fun startAnalysis(bitmap: Bitmap) {
        val uri = _uiState.value.selectedUri ?: return
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null, analysisResult = null) }

        viewModelScope.launch {
            repository.analyzeWarningLight(bitmap).fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(isAnalyzing = false, analysisResult = result) }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isAnalyzing = false, errorMessage = throwable.message ?: "Unknown error occurred") }
                }
            )
        }
    }

    fun saveToHistory(vehicleId: String, onComplete: () -> Unit) {
        val result = _uiState.value.analysisResult ?: return
        if (_uiState.value.isSavedToHistory) {
            onComplete()
            return
        }

        val event = VehicleHistoryEvent(
            vehicleId = vehicleId,
            type = VehicleHistoryEventType.WARNING_ANALYSIS,
            title = "${result.warningName} warning detected",
            description = result.explanation,
            eventDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            confirmedByUser = true
        )

        viewModelScope.launch {
            historyRepository.saveHistoryEvent(vehicleId, event).onSuccess {
                _uiState.update { it.copy(isSavedToHistory = true) }
                onComplete()
            }
        }
    }

    fun resetState() {
        _uiState.update { AiAssistantUiState() }
    }
}
