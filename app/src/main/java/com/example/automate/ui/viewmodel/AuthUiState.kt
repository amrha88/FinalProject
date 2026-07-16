package com.example.automate.ui.viewmodel

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isLoginMode: Boolean = true
)
