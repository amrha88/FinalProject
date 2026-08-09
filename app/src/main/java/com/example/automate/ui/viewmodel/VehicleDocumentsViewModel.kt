package com.example.automate.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleDocument
import com.example.automate.domain.model.VehicleDocumentType
import com.example.automate.domain.repository.VehicleDocumentAnalysisRepository
import com.example.automate.domain.repository.VehicleDocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VehicleDocumentsViewModel(
    private val analysisRepository: VehicleDocumentAnalysisRepository,
    private val documentRepository: VehicleDocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleDocumentsUiState())
    val uiState: StateFlow<VehicleDocumentsUiState> = _uiState.asStateFlow()

    fun loadDocuments(vehicleId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            documentRepository.getDocuments(vehicleId).fold(
                onSuccess = { docs ->
                    _uiState.update { it.copy(isLoading = false, documents = docs) }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = throwable.message) }
                }
            )
        }
    }

    fun onImageSelected(uri: String) {
        _uiState.update { it.copy(selectedImageUri = uri, extraction = null, editableDocument = null) }
    }

    fun removeSelectedImage() {
        _uiState.update { it.copy(selectedImageUri = null, extraction = null, editableDocument = null) }
    }

    fun analyzeDocument(vehicle: Vehicle, bitmap: Bitmap) {
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
        viewModelScope.launch {
            analysisRepository.analyzeDocument(vehicle, bitmap).fold(
                onSuccess = { extraction ->
                    if (extraction.documentDetected) {
                        val editable = VehicleDocument(
                            vehicleId = vehicle.id,
                            documentType = extraction.documentType,
                            documentTitle = extraction.documentTitle,
                            documentDate = extraction.documentDate,
                            vehiclePlate = extraction.vehiclePlate ?: vehicle.plate,
                            mileage = extraction.mileage,
                            garageOrProvider = extraction.garageOrProvider,
                            serviceItems = extraction.serviceItems,
                            oilChanged = extraction.oilChanged,
                            oilFilterChanged = extraction.oilFilterChanged,
                            airFilterChanged = extraction.airFilterChanged,
                            cabinFilterChanged = extraction.cabinFilterChanged,
                            brakeFluidChanged = extraction.brakeFluidChanged,
                            coolantChanged = extraction.coolantChanged,
                            sparkPlugsChanged = extraction.sparkPlugsChanged,
                            brakePadsChanged = extraction.brakePadsChanged,
                            batteryChanged = extraction.batteryChanged,
                            tiresChanged = extraction.tiresChanged,
                            timingBeltChanged = extraction.timingBeltChanged,
                            licenceExpiryDate = extraction.licenceExpiryDate,
                            inspectionDate = extraction.inspectionDate,
                            inspectionExpiryDate = extraction.inspectionExpiryDate,
                            insuranceProvider = extraction.insuranceProvider,
                            policyNumber = extraction.policyNumber,
                            insuranceStartDate = extraction.insuranceStartDate,
                            insuranceExpiryDate = extraction.insuranceExpiryDate,
                            repairsPerformed = extraction.repairsPerformed,
                            partsReplaced = extraction.partsReplaced,
                            nextServiceDate = extraction.nextServiceDate,
                            nextServiceMileage = extraction.nextServiceMileage,
                            summary = extraction.summary,
                            confirmedByUser = false
                        )
                        _uiState.update { it.copy(isAnalyzing = false, extraction = extraction, editableDocument = editable) }
                    } else {
                        _uiState.update { it.copy(isAnalyzing = false, errorMessage = "A vehicle document was not detected in this image.") }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isAnalyzing = false, errorMessage = throwable.message ?: "Analysis failed") }
                }
            )
        }
    }

    fun updateExtractedField(update: (VehicleDocument) -> VehicleDocument) {
        _uiState.update { state ->
            state.copy(editableDocument = state.editableDocument?.let { update(it) })
        }
    }

    fun changeDocumentType(type: VehicleDocumentType) {
        _uiState.update { state ->
            state.copy(editableDocument = state.editableDocument?.copy(documentType = type))
        }
    }

    fun startEditing(document: VehicleDocument) {
        _uiState.update { it.copy(editableDocument = document, isEditingExisting = true, replacingDocumentId = null) }
    }

    fun startReplacing(documentId: String) {
        _uiState.update { it.copy(replacingDocumentId = documentId, isEditingExisting = false, editableDocument = null, selectedImageUri = null, extraction = null) }
    }

    fun saveConfirmedDocument(vehicleId: String) {
        val document = _uiState.value.editableDocument ?: return
        val isEditing = _uiState.value.isEditingExisting
        val replacingId = _uiState.value.replacingDocumentId

        if (document.documentType == VehicleDocumentType.OTHER && document.documentTitle.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Document title is required for 'Other' type") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val confirmedDoc = document.copy(confirmedByUser = true)
            
            val result = when {
                isEditing -> documentRepository.updateDocument(vehicleId, confirmedDoc).map { confirmedDoc.id }
                replacingId != null -> documentRepository.replaceDocument(vehicleId, replacingId, confirmedDoc)
                else -> documentRepository.saveDocument(vehicleId, confirmedDoc)
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, selectedImageUri = null, extraction = null, editableDocument = null, isEditingExisting = false, replacingDocumentId = null) }
                    loadDocuments(vehicleId)
                },
                onFailure = {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "The document could not be saved.") }
                }
            )
        }
    }

    fun deleteDocument(vehicleId: String, documentId: String) {
        _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
        viewModelScope.launch {
            documentRepository.deleteDocument(vehicleId, documentId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isDeleting = false) }
                    loadDocuments(vehicleId)
                },
                onFailure = {
                    _uiState.update { it.copy(isDeleting = false, errorMessage = "The document could not be deleted.") }
                }
            )
        }
    }

    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearSaveSuccess() { _uiState.update { it.copy(saveSuccess = false) } }
}
