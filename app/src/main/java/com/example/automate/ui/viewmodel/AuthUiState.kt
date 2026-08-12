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
    val specsLoadFailed: Boolean = false,
    val isSavingProfile: Boolean = false,
    val profileSaved: Boolean = false,
    val profileError: String? = null,
    val isChangingPassword: Boolean = false,
    val passwordChanged: Boolean = false,
    val changePasswordError: String? = null,
    val isChangingEmail: Boolean = false,
    val emailChangeRequested: Boolean = false,
    val changeEmailError: String? = null,
    val isDeletingAccount: Boolean = false,
    val accountDeleted: Boolean = false,
    val deleteAccountError: String? = null,
    val isSendingPasswordReset: Boolean = false,
    val passwordResetSent: Boolean = false,
    val passwordResetError: String? = null
)
