package com.example.automate.ui.viewmodel

import com.example.automate.domain.model.VehicleDocument
import com.example.automate.domain.model.VehicleDocumentExtraction

data class VehicleDocumentsUiState(
    val documents: List<VehicleDocument> = emptyList(),
    val isLoading: Boolean = false,
    val selectedImageUri: String? = null,
    val isAnalyzing: Boolean = false,
    val extraction: VehicleDocumentExtraction? = null,
    val editableDocument: VehicleDocument? = null,
    val isEditingExisting: Boolean = false,
    val replacingDocumentId: String? = null,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)
