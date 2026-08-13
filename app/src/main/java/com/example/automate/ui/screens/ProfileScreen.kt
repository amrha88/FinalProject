package com.example.automate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.R
import com.example.automate.ui.components.AppTextField
import com.example.automate.ui.components.AvatarImage
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.components.rememberProfileImagePicker
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AuthUiState
import com.example.automate.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val pickImage = rememberProfileImagePicker(onImagePicked = { viewModel.updateProfilePhoto(it) })

    ProfileScreenContent(
        uiState = uiState,
        onAvatarClick = pickImage,
        onSave = { fullName, age, hasLicense ->
            viewModel.clearProfileError()
            viewModel.updateProfile(fullName, age, hasLicense)
        },
        onDismissSaved = { viewModel.clearProfileSaved() },
        onChangeEmail = { currentPassword, newEmail -> viewModel.changeEmail(currentPassword, newEmail) },
        onDismissChangeEmail = {
            viewModel.clearChangeEmailError()
            viewModel.clearEmailChangeRequested()
        },
        onLogout = onLogout,
        bottomBar = bottomBar
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: AuthUiState,
    onAvatarClick: () -> Unit,
    onSave: (String, Int, Boolean) -> Unit,
    onDismissSaved: () -> Unit,
    onChangeEmail: (String, String) -> Unit = { _, _ -> },
    onDismissChangeEmail: () -> Unit = {},
    onLogout: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var fullName by remember { mutableStateOf(uiState.userName ?: "") }
    var age by remember { mutableStateOf(uiState.userAge?.takeIf { it > 0 }?.toString() ?: "") }
    var hasLicense by remember { mutableStateOf(uiState.userHasLicense) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showChangeEmail by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.profileSaved) {
        if (uiState.profileSaved) {
            kotlinx.coroutines.delay(1800)
            onDismissSaved()
        }
    }

    val errorFullName = stringResource(R.string.signup_error_full_name)
    val errorAge = stringResource(R.string.signup_error_age)

    Scaffold(
        containerColor = Color(0xFF000C1F),
        bottomBar = bottomBar
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.profile_title),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.clickable(onClick = onAvatarClick)
            ) {
                AvatarImage(
                    photoBase64 = uiState.userPhotoBase64,
                    name = uiState.userName,
                    size = 96.dp
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF007BFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.cd_change_photo),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = fullName,
            onValueChange = { fullName = it; validationError = null },
            label = stringResource(R.string.label_full_name)
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = age,
            onValueChange = { if (it.length <= 3) { age = it.filter(Char::isDigit); validationError = null } },
            label = stringResource(R.string.label_age),
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = uiState.userEmail ?: "",
            onValueChange = {},
            label = stringResource(R.string.label_email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.profile_change_email),
            color = Color(0xFF4FA8FF),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { showChangeEmail = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.signup_has_license_question),
            color = Color.White,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileLicenseOption(
                text = stringResource(R.string.action_yes),
                selected = hasLicense,
                onClick = { hasLicense = true },
                modifier = Modifier.weight(1f)
            )
            ProfileLicenseOption(
                text = stringResource(R.string.action_no),
                selected = !hasLicense,
                onClick = { hasLicense = false },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val errorText = validationError ?: uiState.profileError
        errorText?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (uiState.profileSaved) {
            Text(
                text = stringResource(R.string.profile_saved),
                color = Color(0xFF4CAF50),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (uiState.isSavingProfile) {
            CircularProgressIndicator(color = Color(0xFF007BFF))
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = stringResource(R.string.action_save_changes),
            enabled = !uiState.isSavingProfile,
            onClick = {
                val ageValue = age.toIntOrNull()
                when {
                    fullName.isBlank() -> validationError = errorFullName
                    ageValue == null || ageValue !in 1..120 -> validationError = errorAge
                    else -> onSave(fullName, ageValue, hasLicense)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
            border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.profile_logout), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
    }

    if (showChangeEmail) {
        ChangeEmailDialog(
            isSaving = uiState.isChangingEmail,
            error = uiState.changeEmailError,
            requested = uiState.emailChangeRequested,
            onDismiss = {
                showChangeEmail = false
                onDismissChangeEmail()
            },
            onConfirm = { currentPassword, newEmail -> onChangeEmail(currentPassword, newEmail) }
        )
    }
}

@Composable
private fun ChangeEmailDialog(
    isSaving: Boolean,
    error: String?,
    requested: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.change_email_dialog_title)) },
        text = {
            Column {
                if (requested) {
                    Text(
                        text = stringResource(R.string.change_email_confirmation_sent, newEmail),
                        color = Color(0xFF4CAF50),
                        fontSize = 13.sp
                    )
                } else {
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text(stringResource(R.string.label_new_email)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text(stringResource(R.string.current_password_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = error, color = Color(0xFFFF5252), fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (!requested) {
                TextButton(
                    enabled = !isSaving,
                    onClick = { onConfirm(currentPassword, newEmail) }
                ) {
                    Text(stringResource(if (isSaving) R.string.action_sending else R.string.action_send_verification))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(if (requested) R.string.action_done else R.string.action_cancel)) }
        }
    )
}

@Composable
private fun ProfileLicenseOption(
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
private fun ProfileScreenPreview() {
    AutomateTheme {
        ProfileScreenContent(
            uiState = AuthUiState(
                userName = "Jane Doe",
                userAge = 28,
                userEmail = "jane.doe@example.com",
                userHasLicense = true
            ),
            onAvatarClick = {},
            onSave = { _, _, _ -> },
            onDismissSaved = {}
        )
    }
}