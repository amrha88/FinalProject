package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AuthUiState
import com.example.automate.ui.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSuccess: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hasLicense by remember { mutableStateOf<Boolean?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess(email)
        }
    }

    SignUpScreenContent(
        uiState = uiState,
        formError = formError,
        fullName = fullName,
        onFullNameChange = { fullName = it; formError = null; viewModel.clearError() },
        age = age,
        onAgeChange = { age = it; formError = null; viewModel.clearError() },
        email = email,
        onEmailChange = { email = it; formError = null; viewModel.clearError() },
        confirmEmail = confirmEmail,
        onConfirmEmailChange = { confirmEmail = it; formError = null; viewModel.clearError() },
        password = password,
        onPasswordChange = { password = it; formError = null; viewModel.clearError() },
        hasLicense = hasLicense,
        onHasLicenseChange = { hasLicense = it; formError = null },
        onBackClick = onBackClick,
        onSubmit = {
            val validationError = validateSignUpForm(
                fullName = fullName,
                age = age,
                email = email,
                confirmEmail = confirmEmail,
                password = password,
                hasLicense = hasLicense
            )
            if (validationError != null) {
                formError = validationError
            } else {
                viewModel.onRegister(email, password)
            }
        }
    )
}

private fun validateSignUpForm(
    fullName: String,
    age: String,
    email: String,
    confirmEmail: String,
    password: String,
    hasLicense: Boolean?
): String? {
    if (fullName.isBlank()) return "Please enter your full name."
    val ageValue = age.toIntOrNull()
    if (ageValue == null || ageValue !in 1..120) return "Please enter a valid age."
    if (email.isBlank()) return "Please enter your email address."
    if (email != confirmEmail) return "Email addresses do not match."
    if (password.length < 6) return "Password must be at least 6 characters."
    if (hasLicense == null) return "Please select whether you have a license."
    return null
}

@Composable
private fun SignUpScreenContent(
    uiState: AuthUiState,
    formError: String?,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    confirmEmail: String,
    onConfirmEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    hasLicense: Boolean?,
    onHasLicenseChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000C1F))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create account",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "Full name"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = age,
            onValueChange = { if (it.length <= 3) onAgeChange(it.filter(Char::isDigit)) },
            label = "Age",
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email address",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = confirmEmail,
            onValueChange = onConfirmEmailChange,
            label = "Confirm email address",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Do you have a driving license?",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LicenseOption(
                text = "Yes",
                selected = hasLicense == true,
                onClick = { onHasLicenseChange(true) },
                modifier = Modifier.weight(1f)
            )
            LicenseOption(
                text = "No",
                selected = hasLicense == false,
                onClick = { onHasLicenseChange(false) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val errorText = formError ?: uiState.error
        errorText?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color(0xFF007BFF))
            Spacer(modifier = Modifier.height(16.dp))
        }

        PrimaryButton(
            text = "Sign up",
            onClick = onSubmit,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LicenseOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) Color(0xFF007BFF) else Color.White.copy(alpha = 0.05f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    AutomateTheme {
        SignUpScreenContent(
            uiState = AuthUiState(),
            formError = null,
            fullName = "",
            onFullNameChange = {},
            age = "",
            onAgeChange = {},
            email = "",
            onEmailChange = {},
            confirmEmail = "",
            onConfirmEmailChange = {},
            password = "",
            onPasswordChange = {},
            hasLicense = null,
            onHasLicenseChange = {},
            onBackClick = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, name = "Filled")
@Composable
private fun SignUpScreenFilledPreview() {
    AutomateTheme {
        SignUpScreenContent(
            uiState = AuthUiState(),
            formError = null,
            fullName = "Jane Doe",
            onFullNameChange = {},
            age = "28",
            onAgeChange = {},
            email = "jane.doe@example.com",
            onEmailChange = {},
            confirmEmail = "jane.doe@example.com",
            onConfirmEmailChange = {},
            password = "password123",
            onPasswordChange = {},
            hasLicense = true,
            onHasLicenseChange = {},
            onBackClick = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun SignUpScreenLoadingPreview() {
    AutomateTheme {
        SignUpScreenContent(
            uiState = AuthUiState(isLoading = true),
            formError = null,
            fullName = "Jane Doe",
            onFullNameChange = {},
            age = "28",
            onAgeChange = {},
            email = "jane.doe@example.com",
            onEmailChange = {},
            confirmEmail = "jane.doe@example.com",
            onConfirmEmailChange = {},
            password = "password123",
            onPasswordChange = {},
            hasLicense = true,
            onHasLicenseChange = {},
            onBackClick = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun SignUpScreenErrorPreview() {
    AutomateTheme {
        SignUpScreenContent(
            uiState = AuthUiState(),
            formError = "Email addresses do not match.",
            fullName = "Jane Doe",
            onFullNameChange = {},
            age = "28",
            onAgeChange = {},
            email = "jane.doe@example.com",
            onEmailChange = {},
            confirmEmail = "jane.doe@x.com",
            onConfirmEmailChange = {},
            password = "password123",
            onPasswordChange = {},
            hasLicense = false,
            onHasLicenseChange = {},
            onBackClick = {},
            onSubmit = {}
        )
    }
}