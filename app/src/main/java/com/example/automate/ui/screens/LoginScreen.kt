package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
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
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

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

        val linkStyle = TextLinkStyles(
            style = SpanStyle(color = Color(0xFF007BFF), textDecoration = TextDecoration.Underline)
        )
        val legalText = buildAnnotatedString {
            append("By continuing you agree to our ")
            withLink(
                LinkAnnotation.Clickable(tag = "terms", styles = linkStyle) {
                    showTermsDialog = true
                }
            ) {
                append("Terms of Service")
            }
            append(" and ")
            withLink(
                LinkAnnotation.Clickable(tag = "privacy", styles = linkStyle) {
                    showPrivacyDialog = true
                }
            ) {
                append("Privacy Policy")
            }
            append(".")
        }

        Text(
            text = legalText,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showTermsDialog) {
        LegalDialog(
            title = "Terms of Service",
            body = TERMS_OF_SERVICE_TEXT,
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showPrivacyDialog) {
        LegalDialog(
            title = "Privacy Policy",
            body = PRIVACY_POLICY_TEXT,
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
private fun LegalDialog(title: String, body: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = body,
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                fontSize = 13.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private const val TERMS_OF_SERVICE_TEXT = """Welcome to Automate. By creating an account or signing in, you agree to these Terms of Service.

1. Use of the App
Automate helps you track vehicle maintenance, licences, and notifications. You agree to provide accurate information about your vehicles and to use the app only for lawful purposes.

2. Account Responsibility
You are responsible for maintaining the confidentiality of your login credentials and for all activity under your account.

3. Service Availability
We aim to keep Automate available at all times but do not guarantee uninterrupted access. Features may change or be discontinued without notice.

4. Limitation of Liability
Automate is provided "as is". We are not liable for any damages arising from missed maintenance reminders, licence renewals, or other notifications.

5. Changes to These Terms
We may update these terms from time to time. Continued use of the app after changes constitutes acceptance of the new terms."""

private const val PRIVACY_POLICY_TEXT = """Your privacy matters to us. This Privacy Policy explains how Automate collects, uses, and protects your information.

1. Information We Collect
We collect the information you provide when signing up (such as name and email) and the vehicle data you add to the app.

2. How We Use Your Information
Your information is used to operate your account, store your vehicle records, and send maintenance and licence notifications.

3. Data Storage
Your data is stored securely using Firebase services. We take reasonable measures to protect it from unauthorized access.

4. Sharing of Information
We do not sell your personal information. Data may be shared with service providers strictly to operate the app (e.g. cloud storage).

5. Your Choices
You can update or delete your account information at any time from within the app.

6. Contact
If you have questions about this policy, please contact us through the app's support channels."""

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
