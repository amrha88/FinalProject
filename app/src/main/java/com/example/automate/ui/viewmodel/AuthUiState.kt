package com.example.automate.ui.viewmodel

import com.example.automate.domain.model.Vehicle

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val userAge: Int? = null,
    val userEmail: String? = null,
    val userHasLicense: Boolean = false,
    val userPhotoBase64: String? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val isSavingVehicle: Boolean = false,
    val vehicleSaved: Boolean = false,
    val vehicleError: String? = null,
    val vehicleDeleted: Boolean = false,
    val isLoadingSpecs: Boolean = false,
    val isSavingProfile: Boolean = false,
    val profileSaved: Boolean = false,
    val profileError: String? = null
)
