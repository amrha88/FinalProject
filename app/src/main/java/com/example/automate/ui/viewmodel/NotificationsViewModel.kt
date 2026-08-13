package com.example.automate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.domain.model.ReminderStatus
import com.example.automate.domain.model.VehicleReminder
import com.example.automate.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
                val activeReminders = allReminders.filter { it.status == ReminderStatus.ACTIVE }
                
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        reminders = sortReminders(activeReminders)
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load notifications.") }
            }
        }
    }

    private fun sortReminders(reminders: List<VehicleReminder>): List<VehicleReminder> {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

        return reminders.sortedWith(compareBy<VehicleReminder> { reminder ->
            val dueDateStr = reminder.dueDate
            if (dueDateStr != null) {
                try {
                    val date = sdf.parse(dueDateStr)
                    if (date != null) {
                        val diff = date.time - now.time
                        val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                        if (days < 0) -1000000 + days // Overdue first
                        else if (days == 0L) -500000 // Due today second
                        else days // Then by date
                    } else 999999
                } catch (e: Exception) {
                    999999
                }
            } else {
                // Handle mileage-only reminders if needed, or put them at the end
                999999
            }
        })
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
