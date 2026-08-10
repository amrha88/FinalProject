package com.example.automate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.data.repository.FirebaseVehicleSpecsRepository
import com.example.automate.data.repository.FirestoreVehicleRepository
import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.UserProfile
import com.example.automate.domain.repository.AuthRepository
import com.example.automate.domain.repository.VehicleRepository
import com.example.automate.domain.repository.VehicleSpecsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(
    private val repository: AuthRepository,
    private val vehicleRepository: VehicleRepository = FirestoreVehicleRepository(),
    private val vehicleSpecsRepository: VehicleSpecsRepository = FirebaseVehicleSpecsRepository()
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

    fun changePassword(currentPassword: String, newPassword: String) {
        if (_uiState.value.isChangingPassword) return

        if (currentPassword.isBlank()) {
            _uiState.update { it.copy(changePasswordError = "Please enter your current password.") }
            return
        }
        if (newPassword.length < 6) {
            _uiState.update { it.copy(changePasswordError = "New password must be at least 6 characters.") }
            return
        }

        _uiState.update { it.copy(isChangingPassword = true, changePasswordError = null) }
        viewModelScope.launch {
            repository.changePassword(currentPassword, newPassword).fold(
                onSuccess = {
                    _uiState.update { it.copy(isChangingPassword = false, passwordChanged = true) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            changePasswordError = throwable.message ?: "Failed to change password."
                        )
                    }
                }
            )
        }
    }

    fun clearPasswordChanged() {
        _uiState.update { it.copy(passwordChanged = false) }
    }

    fun clearChangePasswordError() {
        _uiState.update { it.copy(changePasswordError = null) }
    }

    fun changeEmail(currentPassword: String, newEmail: String) {
        if (_uiState.value.isChangingEmail) return

        if (currentPassword.isBlank()) {
            _uiState.update { it.copy(changeEmailError = "Please enter your current password.") }
            return
        }
        if (!isValidEmail(newEmail)) {
            _uiState.update { it.copy(changeEmailError = "Please enter a valid email address.") }
            return
        }

        _uiState.update { it.copy(isChangingEmail = true, changeEmailError = null) }
        viewModelScope.launch {
            repository.changeEmail(currentPassword, newEmail).fold(
                onSuccess = {
                    _uiState.update { it.copy(isChangingEmail = false, emailChangeRequested = true) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isChangingEmail = false,
                            changeEmailError = throwable.message ?: "Failed to change email."
                        )
                    }
                }
            )
        }
    }

    fun clearEmailChangeRequested() {
        _uiState.update { it.copy(emailChangeRequested = false) }
    }

    fun clearChangeEmailError() {
        _uiState.update { it.copy(changeEmailError = null) }
    }

    fun deleteAccount(currentPassword: String) {
        if (_uiState.value.isDeletingAccount) return

        if (currentPassword.isBlank()) {
            _uiState.update { it.copy(deleteAccountError = "Please enter your current password.") }
            return
        }

        _uiState.update { it.copy(isDeletingAccount = true, deleteAccountError = null) }
        viewModelScope.launch {
            repository.deleteAccount(currentPassword).fold(
                onSuccess = {
                    _uiState.update { it.copy(isDeletingAccount = false, accountDeleted = true) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            deleteAccountError = throwable.message ?: "Failed to delete account."
                        )
                    }
                }
            )
        }
    }

    fun clearDeleteAccountError() {
        _uiState.update { it.copy(deleteAccountError = null) }
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

    fun addVehicle(
        manufacturer: String,
        model: String,
        year: String,
        plate: String,
        transmission: String? = null,
        photoBase64: String? = null
    ) {
        if (_uiState.value.isSavingVehicle) return

        val newVehicle = Vehicle(
            id = UUID.randomUUID().toString(),
            manufacturer = manufacturer,
            model = model,
            year = year,
            plate = plate,
            transmission = transmission,
            photoBase64 = photoBase64
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

    fun updateVehicle(
        vehicleId: String,
        manufacturer: String,
        model: String,
        year: String,
        plate: String,
        transmission: String? = null,
        photoBase64: String? = null
    ) {
        if (_uiState.value.isSavingVehicle) return

        val updatedVehicle = Vehicle(
            id = vehicleId,
            manufacturer = manufacturer,
            model = model,
            year = year,
            plate = plate,
            transmission = transmission,
            photoBase64 = photoBase64
        )
        _uiState.update { it.copy(isSavingVehicle = true, vehicleError = null) }
        viewModelScope.launch {
            vehicleRepository.updateVehicle(updatedVehicle).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isSavingVehicle = false,
                            vehicleSaved = true,
                            vehicles = state.vehicles.map { if (it.id == vehicleId) updatedVehicle else it }
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingVehicle = false,
                            vehicleError = throwable.message ?: "Failed to update vehicle."
                        )
                    }
                }
            )
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            vehicleRepository.deleteVehicle(vehicleId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            vehicleDeleted = true,
                            vehicles = state.vehicles.filterNot { it.id == vehicleId }
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(vehicleError = throwable.message ?: "Failed to delete vehicle.") }
                }
            )
        }
    }

    fun clearVehicleDeleted() {
        _uiState.update { it.copy(vehicleDeleted = false) }
    }

    fun loadSpecsIfNeeded(vehicleId: String) {
        val vehicle = _uiState.value.vehicles.find { it.id == vehicleId } ?: return
        if (vehicle.specs != null || _uiState.value.isLoadingSpecs) return

        _uiState.update { it.copy(isLoadingSpecs = true, specsLoadFailed = false) }
        viewModelScope.launch {
            vehicleSpecsRepository.getSpecs(vehicle).fold(
                onSuccess = { specs ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingSpecs = false,
                            vehicles = state.vehicles.map { if (it.id == vehicleId) it.copy(specs = specs) else it }
                        )
                    }
                    vehicleRepository.saveSpecs(vehicleId, specs)
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingSpecs = false, specsLoadFailed = true) }
                }
            )
        }
    }

    fun selectEngineVariant(vehicleId: String, variant: com.example.automate.domain.model.EngineVariant) {
        val vehicle = _uiState.value.vehicles.find { it.id == vehicleId } ?: return
        val currentSpecs = vehicle.specs ?: return
        val updatedSpecs = currentSpecs.withSelectedVariant(variant)

        _uiState.update { state ->
            state.copy(vehicles = state.vehicles.map { if (it.id == vehicleId) it.copy(specs = updatedSpecs) else it })
        }
        viewModelScope.launch {
            vehicleRepository.saveSpecs(vehicleId, updatedSpecs)
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
