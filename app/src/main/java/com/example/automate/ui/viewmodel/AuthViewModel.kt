package com.example.automate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automate.domain.model.UserProfile
import com.example.automate.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

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
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
            )
        }
    }

    fun onRegister(profile: UserProfile, password: String) {
        if (_uiState.value.isLoading) return

        if (!isValidEmail(profile.email)) {
            _uiState.update { it.copy(error = "Please enter a valid email address.") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            repository.register(profile, password).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
            )
        }
    }

    fun checkEmailVerification(onVerified: () -> Unit) {
        if (_uiState.value.isCheckingVerification) return

        _uiState.update { it.copy(isCheckingVerification = true, verificationMessage = null) }

        viewModelScope.launch {
            val verified = repository.isCurrentUserEmailVerified()
            _uiState.update { it.copy(isCheckingVerification = false) }
            if (verified) {
                onVerified()
            } else {
                _uiState.update {
                    it.copy(verificationMessage = "Your email isn't verified yet. Check your inbox, tap the link, then try again.")
                }
            }
        }
    }

    fun resendVerificationEmail() {
        if (_uiState.value.isCheckingVerification) return

        _uiState.update { it.copy(isCheckingVerification = true, verificationMessage = null) }

        viewModelScope.launch {
            repository.resendVerificationEmail().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isCheckingVerification = false, verificationMessage = "Verification email resent.")
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isCheckingVerification = false, verificationMessage = throwable.message) }
                }
            )
        }
    }

    fun resetState() {
        _uiState.update { AuthUiState() }
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
