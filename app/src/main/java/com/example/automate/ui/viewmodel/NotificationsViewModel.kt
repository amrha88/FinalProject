package com.example.automate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun loadReminders(vehicleId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val vehicleResult = reminderRepository.getVehicleReminders(vehicleId)
            val userResult = reminderRepository.getUserReminders()
            
            if (vehicleResult.isSuccess && userResult.isSuccess) {
                val allReminders = (vehicleResult.getOrNull() ?: emptyList()) + (userResult.getOrNull() ?: emptyList())
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        reminders = sortReminders(allReminders)
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load notifications.") }
            }
        }
    }

    private fun sortReminders(reminders: List<com.example.automate.domain.model.VehicleReminder>): List<com.example.automate.domain.model.VehicleReminder> {
        // Simple sort by due date for now
        return reminders.sortedBy { it.dueDate ?: "9999-12-31" }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
