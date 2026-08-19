package com.example.automate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.data.repository.FirestoreReminderRepository
import com.example.automate.domain.engine.ReminderEngine
import com.example.automate.domain.model.DatePrecision
import com.example.automate.domain.model.ReminderType
import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleLicence
import com.example.automate.domain.model.VehicleLicenceExtraction
import com.example.automate.domain.repository.ReminderRepository
import com.example.automate.domain.repository.VehicleLicenceAnalysisRepository
import com.example.automate.domain.repository.VehicleLicenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.graphics.Bitmap

class VehicleLicenceViewModel(
    private val analysisRepository: VehicleLicenceAnalysisRepository,
    private val licenceRepository: VehicleLicenceRepository,
    private val reminderRepository: ReminderRepository = FirestoreReminderRepository()
) : ViewModel() {

    private val reminderEngine = ReminderEngine(reminderRepository)

    private val _uiState = MutableStateFlow(VehicleLicenceUiState())
    val uiState: StateFlow<VehicleLicenceUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: String) {
        _uiState.update { it.copy(selectedImageUri = uri, extraction = null, editableLicence = null) }
    }

    fun removeSelectedImage() {
        _uiState.update { it.copy(selectedImageUri = null, extraction = null, editableLicence = null) }
    }

    fun analyzeSelectedImage(vehicle: Vehicle, bitmap: Bitmap) {
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
        viewModelScope.launch {
            analysisRepository.analyzeLicenceImage(vehicle, bitmap).fold(
                onSuccess = { extraction ->
                    if (extraction.documentDetected) {
                        val editable = VehicleLicence(
                            ownerName = extraction.ownerName,
                            licencePlate = extraction.licencePlate ?: vehicle.plate,
                            manufacturer = extraction.manufacturer ?: vehicle.manufacturer,
                            model = extraction.model ?: vehicle.model,
                            modelCode = extraction.modelCode,
                            year = extraction.year ?: vehicle.year,
                            color = extraction.color,
                            chassisNumber = extraction.chassisNumber,
                            engineNumber = extraction.engineNumber,
                            ownershipDate = extraction.ownershipDate,
                            registrationDate = extraction.registrationDate,
                            licenceExpiryDate = extraction.licenceExpiryDate,
                            inspectionExpiryDate = extraction.inspectionExpiryDate,
                            fuelType = extraction.fuelType
                        )
                        _uiState.update { it.copy(isAnalyzing = false, extraction = extraction, editableLicence = editable) }
                    } else {
                        _uiState.update { it.copy(isAnalyzing = false, errorMessage = "A vehicle licence was not detected in this image.") }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isAnalyzing = false, errorMessage = throwable.message ?: "Analysis failed") }
                }
            )
        }
    }

    fun loadLicence(vehicleId: String) {
        viewModelScope.launch {
            licenceRepository.loadLicence(vehicleId).fold(
                onSuccess = { licence ->
                    _uiState.update { it.copy(savedLicence = licence) }
                },
                onFailure = { /* Handle error */ }
            )
        }
    }

    fun saveLicence(vehicleId: String) {
        val licence = _uiState.value.editableLicence ?: return
        if (licence.licencePlate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Licence plate is required") }
            return
        }
        
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            licenceRepository.saveLicence(vehicleId, licence).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, savedLicence = licence, selectedImageUri = null, extraction = null, editableLicence = null) }
                    val expiryDate = licence.licenceExpiryDate
                    if (!expiryDate.isNullOrBlank()) {
                        reminderEngine.syncRemindersFromManual(
                            vehicleId = vehicleId,
                            type = ReminderType.VEHICLE_LICENCE,
                            title = "Vehicle Licence",
                            dueDate = expiryDate,
                            precision = DatePrecision.EXACT
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "The licence details could not be saved.") }
                }
            )
        }
    }

    fun deleteLicence(vehicleId: String) {
        viewModelScope.launch {
            licenceRepository.deleteLicence(vehicleId).fold(
                onSuccess = {
                    _uiState.update { it.copy(savedLicence = null) }
                    reminderRepository.deactiveOldReminders(vehicleId, ReminderType.VEHICLE_LICENCE)
                },
                onFailure = {
                    _uiState.update { it.copy(errorMessage = "The licence could not be deleted.") }
                }
            )
        }
    }

    fun startEditing() {
        val saved = _uiState.value.savedLicence ?: return
        _uiState.update { it.copy(editableLicence = saved) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editableLicence = null, selectedImageUri = null, extraction = null) }
    }

    fun updateOwnerName(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(ownerName = value)) } }
    fun updateLicencePlate(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(licencePlate = value)) } }
    fun updateManufacturer(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(manufacturer = value)) } }
    fun updateModel(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(model = value)) } }
    fun updateModelCode(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(modelCode = value)) } }
    fun updateYear(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(year = value)) } }
    fun updateColor(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(color = value)) } }
    fun updateChassisNumber(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(chassisNumber = value)) } }
    fun updateEngineNumber(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(engineNumber = value)) } }
    fun updateOwnershipDate(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(ownershipDate = value)) } }
    fun updateRegistrationDate(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(registrationDate = value)) } }
    fun updateLicenceExpiryDate(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(licenceExpiryDate = value)) } }
    fun updateInspectionExpiryDate(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(inspectionExpiryDate = value)) } }
    fun updateFuelType(value: String) { _uiState.update { it.copy(editableLicence = it.editableLicence?.copy(fuelType = value)) } }

    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearSaveSuccess() { _uiState.update { it.copy(saveSuccess = false) } }
}
