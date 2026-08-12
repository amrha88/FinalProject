package com.example.automate.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.domain.model.*
import com.example.automate.domain.repository.VehicleHistoryAnalysisRepository
import com.example.automate.domain.repository.VehicleHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class VehicleHistoryViewModel(
    private val historyRepository: VehicleHistoryRepository,
    private val analysisRepository: VehicleHistoryAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleHistoryUiState())
    val uiState: StateFlow<VehicleHistoryUiState> = _uiState.asStateFlow()

    private val reminderEngine = com.example.automate.domain.engine.ReminderEngine(com.example.automate.data.repository.FirestoreReminderRepository())

    fun loadHistory(vehicleId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            // Load history events
            historyRepository.loadHistory(vehicleId).fold(
                onSuccess = { events ->
                    _uiState.update { state ->
                        state.copy(
                            historyEvents = events,
                            filteredEvents = applyFilter(events, state.selectedFilter)
                        )
                    }
                },
                onFailure = { /* handled by general loading */ }
            )
            
            // Load maintenance states
            historyRepository.loadMaintenanceState(vehicleId).fold(
                onSuccess = { states ->
                    _uiState.update { it.copy(maintenanceStates = states, isLoading = false) }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Vehicle history could not be loaded.") }
                }
            )
        }
    }

    fun setFilter(filter: VehicleHistoryEventType?) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                filteredEvents = applyFilter(state.historyEvents, filter)
            )
        }
    }

    private fun applyFilter(events: List<VehicleHistoryEvent>, filter: VehicleHistoryEventType?): List<VehicleHistoryEvent> {
        return if (filter == null) events else events.filter { it.type == filter }
    }

    fun onImageSelected(uri: String) {
        _uiState.update { it.copy(selectedImageUri = uri, extraction = null, editableEvent = null) }
    }

    fun removeSelectedImage() {
        _uiState.update { it.copy(selectedImageUri = null, extraction = null, editableEvent = null) }
    }

    fun analyzeMaintenanceImage(vehicle: Vehicle, bitmap: Bitmap) {
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
        viewModelScope.launch {
            analysisRepository.analyzeMaintenanceImage(vehicle, bitmap).fold(
                onSuccess = { extraction ->
                    if (extraction.documentDetected) {
                        val event = convertExtractionToEvent(vehicle.id, extraction)
                        _uiState.update { it.copy(isAnalyzing = false, extraction = extraction, editableEvent = event) }
                    } else {
                        _uiState.update { it.copy(isAnalyzing = false, errorMessage = "The maintenance document could not be analyzed.") }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isAnalyzing = false, errorMessage = "AI analysis failed.") }
                }
            )
        }
    }

    fun analyzeQuickUpdateText(userInput: String) {
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            analysisRepository.analyzeQuickUpdateText(userInput, today).fold(
                onSuccess = { extraction ->
                    val update = ConfirmedMaintenanceUpdate(
                        eventDate = extraction.eventDate ?: today,
                        mileage = extraction.mileage,
                        items = extraction.items,
                        notes = extraction.notes,
                        garageName = extraction.garageName
                    )
                    _uiState.update { it.copy(isAnalyzing = false, textExtraction = extraction, maintenanceUpdate = update) }
                },
                onFailure = {
                    _uiState.update { it.copy(isAnalyzing = false, errorMessage = "AI text analysis failed.") }
                }
            )
        }
    }

    private fun convertExtractionToEvent(vehicleId: String, extraction: MaintenanceExtraction): VehicleHistoryEvent {
        val maintenanceItems = mutableListOf<MaintenanceItem>()
        if (extraction.engineOilChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.ENGINE_OIL, "Replaced"))
        if (extraction.oilFilterChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.OIL_FILTER, "Replaced"))
        if (extraction.airFilterChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.AIR_FILTER, "Replaced"))
        if (extraction.cabinFilterChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.CABIN_FILTER, "Replaced"))

        return VehicleHistoryEvent(
            vehicleId = vehicleId,
            type = VehicleHistoryEventType.MAINTENANCE,
            title = extraction.garageName ?: "Maintenance Service",
            description = extraction.notes,
            eventDate = extraction.serviceDate,
            mileage = extraction.mileage,
            garageName = extraction.garageName,
            servicesPerformed = extraction.servicesPerformed,
            partsReplaced = extraction.partsReplaced,
            maintenanceItems = maintenanceItems,
            nextServiceDate = extraction.nextServiceDate,
            nextServiceMileage = extraction.nextServiceMileage,
            totalAmount = extraction.totalAmount,
            confirmedByUser = false
        )
    }

    fun updateMaintenanceUpdate(updateFunc: (ConfirmedMaintenanceUpdate) -> ConfirmedMaintenanceUpdate) {
        _uiState.update { state ->
            state.copy(maintenanceUpdate = state.maintenanceUpdate?.let { updateFunc(it) })
        }
    }

    fun startComponentUpdate(vehicleId: String, component: MaintenanceItemType, currentMileage: Int?) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val update = ConfirmedMaintenanceUpdate(
            eventDate = today,
            mileage = currentMileage,
            items = listOf(MaintenanceTextItem(component, MaintenanceAction.REPLACED))
        )
        _uiState.update { it.copy(maintenanceUpdate = update, editableEvent = null, selectedImageUri = null) }
    }

    fun saveMaintenanceUpdate(vehicleId: String) {
        val update = _uiState.value.maintenanceUpdate ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            historyRepository.saveMaintenanceUpdate(vehicleId, update).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, maintenanceUpdate = null) }
                    loadHistory(vehicleId)
                    
                    // Sync with Reminder Engine
                    // (Note: Currently saveMaintenanceUpdate doesn't return the eventId,
                    // but syncRemindersFromManual can be used or we can fetch the latest event)
                    // For now, if explicit next service info exists in the update, sync it.
                    // (Need to extend ConfirmedMaintenanceUpdate if nextService info is there)
                },
                onFailure = {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "The maintenance could not be saved.") }
                }
            )
        }
    }

    fun updateEditableEvent(update: (VehicleHistoryEvent) -> VehicleHistoryEvent) {
        _uiState.update { state ->
            state.copy(editableEvent = state.editableEvent?.let { update(it) })
        }
    }

    fun saveHistoryEvent(vehicleId: String) {
        val event = _uiState.value.editableEvent ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val finalEvent = event.copy(confirmedByUser = true)
            historyRepository.saveHistoryEvent(vehicleId, finalEvent).fold(
                onSuccess = { eventId ->
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, selectedImageUri = null, editableEvent = null) }
                    loadHistory(vehicleId)
                    
                    // Sync with Reminder Engine
                    viewModelScope.launch {
                        reminderEngine.syncRemindersFromHistory(vehicleId, finalEvent.copy(id = eventId))
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "The history event could not be saved.") }
                }
            )
        }
    }

    fun startManualEntry(vehicleId: String) {
        val event = VehicleHistoryEvent(
            vehicleId = vehicleId,
            type = VehicleHistoryEventType.MANUAL,
            confirmedByUser = true
        )
        _uiState.update { it.copy(editableEvent = event, selectedImageUri = null, extraction = null) }
    }

    fun cancelEntry() {
        _uiState.update { it.copy(editableEvent = null, maintenanceUpdate = null, selectedImageUri = null, extraction = null, textExtraction = null) }
    }

    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearSaveSuccess() { _uiState.update { it.copy(saveSuccess = false) } }

    fun deleteHistoryEvent(vehicleId: String, eventId: String) {
        viewModelScope.launch {
            historyRepository.deleteHistoryEvent(vehicleId, eventId).fold(
                onSuccess = { loadHistory(vehicleId) },
                onFailure = { _uiState.update { it.copy(errorMessage = "The history event could not be deleted.") } }
            )
        }
    }
}
