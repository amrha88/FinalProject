package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.R
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.AuthViewModel
import com.example.automate.util.AppLanguage
import com.example.automate.util.LocaleManager

@Composable
fun SettingsScreen(
    viewModel: AuthViewModel,
    onProfileClick: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var showChangePassword by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.passwordChanged) {
        if (uiState.passwordChanged) {
            kotlinx.coroutines.delay(1200)
            showChangePassword = false
            viewModel.clearPasswordChanged()
        }
    }

    LaunchedEffect(uiState.accountDeleted) {
        if (uiState.accountDeleted) {
            onAccountDeleted()
        }
    }

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
                text = stringResource(R.string.settings_title),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            SettingsRow(
                icon = Icons.Default.Person,
                title = stringResource(R.string.settings_profile_title),
                subtitle = stringResource(R.string.settings_profile_subtitle),
                onClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Default.Language,
                title = stringResource(R.string.settings_language_title),
                subtitle = stringResource(R.string.settings_language_subtitle),
                onClick = { showLanguagePicker = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.settings_change_password_title),
                subtitle = stringResource(R.string.settings_change_password_subtitle),
                onClick = {
                    viewModel.clearChangePasswordError()
                    showChangePassword = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Default.DeleteForever,
                title = stringResource(R.string.settings_delete_account_title),
                subtitle = stringResource(R.string.settings_delete_account_subtitle),
                iconTint = Color(0xFFFF5252),
                onClick = {
                    viewModel.clearDeleteAccountError()
                    showDeleteAccount = true
                }
            )
        }
    }

    if (showLanguagePicker) {
        val context = LocalContext.current
        LanguagePickerDialog(
            onDismiss = { showLanguagePicker = false },
            onSelect = { language ->
                showLanguagePicker = false
                LocaleManager.setLanguage(context, language)
            }
        )
    }

    if (showChangePassword) {
        ChangePasswordDialog(
            isSaving = uiState.isChangingPassword,
            error = uiState.changePasswordError,
            success = uiState.passwordChanged,
            onDismiss = {
                showChangePassword = false
                viewModel.clearChangePasswordError()
            },
            onConfirm = { current, new -> viewModel.changePassword(current, new) }
        )
    }

    if (showDeleteAccount) {
        DeleteAccountDialog(
            isDeleting = uiState.isDeletingAccount,
            error = uiState.deleteAccountError,
            onDismiss = {
                showDeleteAccount = false
                viewModel.clearDeleteAccountError()
            },
            onConfirm = { password -> viewModel.deleteAccount(password) }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = Color(0xFF4FA8FF)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    onDismiss: () -> Unit,
    onSelect: (AppLanguage) -> Unit
) {
    val current = LocaleManager.currentLanguage()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language_title)) },
        text = {
            Column {
                LanguageOptionRow(
                    label = stringResource(R.string.lang_english),
                    selected = current == AppLanguage.ENGLISH,
                    onClick = { onSelect(AppLanguage.ENGLISH) }
                )
                LanguageOptionRow(
                    label = stringResource(R.string.lang_hebrew),
                    selected = current == AppLanguage.HEBREW,
                    onClick = { onSelect(AppLanguage.HEBREW) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        }
    )
}

@Composable
private fun LanguageOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

@Composable
private fun ChangePasswordDialog(
    isSaving: Boolean,
    error: String?,
    success: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_change_password_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text(stringResource(R.string.current_password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(R.string.new_password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = Color(0xFFFF5252), fontSize = 13.sp)
                }
                if (success) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.password_updated), color = Color(0xFF4CAF50), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = { onConfirm(currentPassword, newPassword) }
            ) {
                Text(stringResource(if (isSaving) R.string.action_saving else R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun DeleteAccountDialog(
    isDeleting: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_account_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.delete_account_warning))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
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
        },
        confirmButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = { onConfirm(password) }
            ) {
                Text(
                    text = stringResource(if (isDeleting) R.string.action_deleting else R.string.action_delete),
                    color = Color(0xFFFF5252)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AutomateTheme {
        // Preview intentionally omitted: SettingsScreen now requires a live AuthViewModel.
    }
}
