package com.example.automate.ui.viewmodel

import com.example.automate.domain.model.*

data class VehicleHistoryUiState(
    val historyEvents: List<VehicleHistoryEvent> = emptyList(),
    val filteredEvents: List<VehicleHistoryEvent> = emptyList(),
    val maintenanceStates: List<VehicleMaintenanceItemState> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: VehicleHistoryEventType? = null,
    
    val isAnalyzing: Boolean = false,
    val extraction: MaintenanceExtraction? = null,
    val textExtraction: MaintenanceTextExtraction? = null,
    val editableEvent: VehicleHistoryEvent? = null,
    val maintenanceUpdate: ConfirmedMaintenanceUpdate? = null,
    val selectedImageUri: String? = null,
    
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)
