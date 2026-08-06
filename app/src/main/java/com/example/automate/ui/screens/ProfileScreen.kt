package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val pickImage = rememberProfileImagePicker(onImagePicked = { viewModel.updateProfilePhoto(it) })

    ProfileScreenContent(
        uiState = uiState,
        onAvatarClick = pickImage,
        onBackClick = onBackClick,
        onSave = { fullName, age, hasLicense ->
            viewModel.clearProfileError()
            viewModel.updateProfile(fullName, age, hasLicense)
        },
        onDismissSaved = { viewModel.clearProfileSaved() },
        bottomBar = bottomBar
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: AuthUiState,
    onAvatarClick: () -> Unit,
    onBackClick: () -> Unit,
    onSave: (String, Int, Boolean) -> Unit,
    onDismissSaved: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    var fullName by remember { mutableStateOf(uiState.userName ?: "") }
    var age by remember { mutableStateOf(uiState.userAge?.takeIf { it > 0 }?.toString() ?: "") }
    var hasLicense by remember { mutableStateOf(uiState.userHasLicense) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.profileSaved) {
        if (uiState.profileSaved) {
            kotlinx.coroutines.delay(1800)
            onDismissSaved()
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
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your profile",
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
                        contentDescription = "Change photo",
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
            label = "Full name"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = age,
            onValueChange = { if (it.length <= 3) { age = it.filter(Char::isDigit); validationError = null } },
            label = "Age",
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = uiState.userEmail ?: "",
            onValueChange = {},
            label = "Email address"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Do you have a driving license?",
            color = Color.White,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileLicenseOption(
                text = "Yes",
                selected = hasLicense,
                onClick = { hasLicense = true },
                modifier = Modifier.weight(1f)
            )
            ProfileLicenseOption(
                text = "No",
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
                text = "Profile saved.",
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
            text = "Save changes",
            enabled = !uiState.isSavingProfile,
            onClick = {
                val ageValue = age.toIntOrNull()
                when {
                    fullName.isBlank() -> validationError = "Please enter your full name."
                    ageValue == null || ageValue !in 1..120 -> validationError = "Please enter a valid age."
                    else -> onSave(fullName, ageValue, hasLicense)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
    }
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
            onBackClick = {},
            onSave = { _, _, _ -> },
            onDismissSaved = {}
        )
    }
}