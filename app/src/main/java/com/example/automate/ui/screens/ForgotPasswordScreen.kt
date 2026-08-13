package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.R
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AuthUiState
import com.example.automate.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearPasswordResetState() }
    }

    ForgotPasswordContent(
        uiState = uiState,
        email = email,
        onEmailChange = { email = it; viewModel.clearPasswordResetState() },
        onSendClick = { viewModel.onForgotPassword(email) },
        onBackClick = onBackClick,
        onBackToSignIn = onBackToSignIn
    )
}

@Composable
private fun ForgotPasswordContent(
    uiState: AuthUiState,
    email: String,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit,
    onBackToSignIn: () -> Unit
) {
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
                    contentDescription = stringResource(R.string.action_back),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.forgot_password_title),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.forgot_password_subtitle),
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.label_email),
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.passwordResetSent) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.forgot_password_sent, email),
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        uiState.passwordResetError?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (uiState.isSendingPasswordReset) {
            CircularProgressIndicator(color = Color(0xFF007BFF))
            Spacer(modifier = Modifier.height(16.dp))
        }

        PrimaryButton(
            text = stringResource(if (uiState.passwordResetSent) R.string.forgot_password_resend else R.string.forgot_password_send),
            onClick = onSendClick,
            enabled = email.isNotBlank() && !uiState.isSendingPasswordReset
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.forgot_password_remembered),
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = stringResource(R.string.forgot_password_sign_in_link),
                color = Color(0xFF007BFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onBackToSignIn)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    AutomateTheme {
        ForgotPasswordContent(
            uiState = AuthUiState(),
            email = "",
            onEmailChange = {},
            onSendClick = {},
            onBackClick = {},
            onBackToSignIn = {}
        )
    }
}

@Preview(showBackground = true, name = "Sent")
@Composable
private fun ForgotPasswordScreenSentPreview() {
    AutomateTheme {
        ForgotPasswordContent(
            uiState = AuthUiState(passwordResetSent = true),
            email = "jane.doe@example.com",
            onEmailChange = {},
            onSendClick = {},
            onBackClick = {},
            onBackToSignIn = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun ForgotPasswordScreenErrorPreview() {
    AutomateTheme {
        ForgotPasswordContent(
            uiState = AuthUiState(passwordResetError = "No account found with this email."),
            email = "jane.doe@example.com",
            onEmailChange = {},
            onSendClick = {},
            onBackClick = {},
            onBackToSignIn = {}
        )
    }
}