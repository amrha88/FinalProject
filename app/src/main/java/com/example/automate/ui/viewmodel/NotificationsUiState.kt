package com.example.automate.ui.viewmodel

import com.example.automate.domain.model.VehicleReminder

data class NotificationsUiState(
    val reminders: List<VehicleReminder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
