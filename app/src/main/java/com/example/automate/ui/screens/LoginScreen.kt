package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AuthUiState
import com.example.automate.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSuccess: (String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess(email)
        }
    }

    LoginScreenContent(
        uiState = uiState,
        email = email,
        onEmailChange = { email = it; viewModel.clearError() },
        password = password,
        onPasswordChange = { password = it; viewModel.clearError() },
        onForgotPassword = { viewModel.onForgotPassword(email) },
        onLogin = { viewModel.onLogin(email, password) },
        onNavigateToSignUp = onNavigateToSignUp,
        onBackClick = onBackClick
    )
}

@Composable
private fun LoginScreenContent(
    uiState: AuthUiState,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onForgotPassword: () -> Unit,
    onLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onBackClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000C1F))
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

        // Logo Placeholder
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.Gray, shape = MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text("Logo", color = Color.White, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Sign in",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))


        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email address",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.Gray
                    )
                }
            }
        )

        Text(
            text = "Forgot password?",
            color = Color(0xFF007BFF),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
                .clickable(onClick = onForgotPassword)
        )

        Spacer(modifier = Modifier.height(24.dp))

        uiState.error?.let { error ->
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
            text = "Sign in",
            onClick = onLogin,
            enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Don't have an account? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "Sign up",
                color = Color(0xFF007BFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onNavigateToSignUp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "By continuing you agree to our Terms of Service and Privacy Policy.",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    AutomateTheme {
        LoginScreenContent(
            uiState = AuthUiState(),
            email = "",
            onEmailChange = {},
            password = "",
            onPasswordChange = {},
            onForgotPassword = {},
            onLogin = {},
            onNavigateToSignUp = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun LoginScreenLoadingPreview() {
    AutomateTheme {
        LoginScreenContent(
            uiState = AuthUiState(isLoading = true),
            email = "user@example.com",
            onEmailChange = {},
            password = "password123",
            onPasswordChange = {},
            onForgotPassword = {},
            onLogin = {},
            onNavigateToSignUp = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun LoginScreenErrorPreview() {
    AutomateTheme {
        LoginScreenContent(
            uiState = AuthUiState(error = "Please enter a valid email address."),
            email = "not-an-email",
            onEmailChange = {},
            password = "123",
            onPasswordChange = {},
            onForgotPassword = {},
            onLogin = {},
            onNavigateToSignUp = {},
            onBackClick = {}
        )
    }
}
