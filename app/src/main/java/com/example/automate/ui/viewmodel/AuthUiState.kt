package com.example.automate.ui.viewmodel

import com.example.automate.domain.model.Vehicle

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val vehicles: List<Vehicle> = emptyList()
)
