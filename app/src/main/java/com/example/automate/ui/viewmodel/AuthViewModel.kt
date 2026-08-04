package com.example.automate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.data.repository.FirestoreVehicleRepository
import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.UserProfile
import com.example.automate.domain.repository.AuthRepository
import com.example.automate.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(
    private val repository: AuthRepository,
    private val vehicleRepository: VehicleRepository = FirestoreVehicleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onLogin(email: String, password: String) {
        if (_uiState.value.isLoading) return

        if (!isValidEmail(email)) {
            _uiState.update { it.copy(error = "Please enter a valid email address.") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            repository.login(email, password).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    loadUserProfile()
                    loadVehicles()
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
            )
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            repository.getCurrentUserProfile().onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        userName = profile.fullName,
                        userAge = profile.age,
                        userEmail = profile.email,
                        userHasLicense = profile.hasLicense,
                        userPhotoBase64 = profile.photoBase64
                    )
                }
            }
        }
    }

    fun updateProfile(fullName: String, age: Int, hasLicense: Boolean) {
        if (_uiState.value.isSavingProfile) return

        val current = _uiState.value
        val profile = UserProfile(
            fullName = fullName,
            age = age,
            email = current.userEmail.orEmpty(),
            hasLicense = hasLicense,
            photoBase64 = current.userPhotoBase64
        )
        _uiState.update { it.copy(isSavingProfile = true, profileError = null) }
        viewModelScope.launch {
            repository.updateCurrentUserProfile(profile).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSavingProfile = false,
                            profileSaved = true,
                            userName = fullName,
                            userAge = age,
                            userHasLicense = hasLicense
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingProfile = false,
                            profileError = throwable.message ?: "Failed to save profile."
                        )
                    }
                }
            )
        }
    }

    fun updateProfilePhoto(photoBase64: String) {
        val current = _uiState.value
        val profile = UserProfile(
            fullName = current.userName.orEmpty(),
            age = current.userAge ?: 0,
            email = current.userEmail.orEmpty(),
            hasLicense = current.userHasLicense,
            photoBase64 = photoBase64
        )
        _uiState.update { it.copy(userPhotoBase64 = photoBase64) }
        viewModelScope.launch {
            repository.updateCurrentUserProfile(profile)
        }
    }

    fun clearProfileSaved() {
        _uiState.update { it.copy(profileSaved = false) }
    }

    fun clearProfileError() {
        _uiState.update { it.copy(profileError = null) }
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getVehicles().onSuccess { vehicles ->
                _uiState.update { it.copy(vehicles = vehicles) }
            }
        }
    }

    fun onRegister(
        email: String, 
        password: String, 
        fullName: String, 
        age: Int, 
        hasLicense: Boolean
    ) {
        if (_uiState.value.isLoading) return

        if (fullName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your full name.") }
            return
        }

        if (!isValidEmail(email)) {
            _uiState.update { it.copy(error = "Please enter a valid email address.") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        val profile = UserProfile(
            fullName = fullName,
            age = age,
            email = email,
            hasLicense = hasLicense
        )

        viewModelScope.launch {
            repository.register(profile, password).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            userName = fullName,
                            userAge = age,
                            userEmail = email,
                            userHasLicense = hasLicense
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
            )
        }
    }

    fun addVehicle(manufacturer: String, model: String, year: String, plate: String) {
        if (_uiState.value.isSavingVehicle) return

        val newVehicle = Vehicle(
            id = UUID.randomUUID().toString(),
            manufacturer = manufacturer,
            model = model,
            year = year,
            plate = plate
        )
        _uiState.update { it.copy(isSavingVehicle = true, vehicleError = null) }
        viewModelScope.launch {
            vehicleRepository.addVehicle(newVehicle).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isSavingVehicle = false,
                            vehicleSaved = true,
                            vehicles = state.vehicles + newVehicle
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingVehicle = false,
                            vehicleError = throwable.message ?: "Failed to save vehicle."
                        )
                    }
                }
            )
        }
    }

    fun clearVehicleSaved() {
        _uiState.update { it.copy(vehicleSaved = false) }
    }

    fun clearVehicleError() {
        _uiState.update { it.copy(vehicleError = null) }
    }

    fun resetState() {
        _uiState.update { AuthUiState() }
    }

    fun logout() {
        repository.logout()
        resetState()
    }

    fun onForgotPassword(email: String) {
        if (email.isBlank() || !isValidEmail(email)) {
            _uiState.update { it.copy(error = "Please enter a valid email to reset password.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            repository.sendPasswordReset(email).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, error = "Password reset email sent.") }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
