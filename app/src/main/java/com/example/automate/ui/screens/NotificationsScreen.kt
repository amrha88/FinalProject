package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.domain.model.VehicleLicence
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.VehicleLicenceViewModel
import java.util.Calendar

@Composable
fun NotificationsScreen(
    vehicleId: String,
    viewModel: VehicleLicenceViewModel,
    onBackClick: () -> Unit,
    onAddLicenceClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehicleId) {
        viewModel.loadLicence(vehicleId)
    }

    NotificationsContent(
        licence = uiState.savedLicence,
        onBackClick = onBackClick,
        onAddLicenceClick = onAddLicenceClick
    )
}

@Composable
fun NotificationsContent(
    licence: VehicleLicence?,
    onBackClick: () -> Unit,
    onAddLicenceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000C1F))
            .statusBarsPadding()
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
            text = "Notifications",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Maintenance and licence reminders",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (licence?.licenceExpiryDate.isNullOrBlank() && licence?.inspectionExpiryDate.isNullOrBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(20.dp)
            ) {
                Text(
                    text = "No reminders yet",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Add this vehicle's licence to get expiry reminders for registration and inspection.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(text = "Add licence details", onClick = onAddLicenceClick)
            }
        } else {
            ReminderCard(title = "Licence expiry", dateString = licence?.licenceExpiryDate)
            Spacer(modifier = Modifier.height(12.dp))
            ReminderCard(title = "Inspection expiry", dateString = licence?.inspectionExpiryDate)
        }
    }
}

private data class ReminderStatus(
    val label: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun daysUntil(dateString: String): Int? {
    val parts = dateString.trim().split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null

    val target = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day)
    }
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffMillis = target.timeInMillis - today.timeInMillis
    return (diffMillis / (24 * 60 * 60 * 1000)).toInt()
}

private fun statusFor(days: Int?): ReminderStatus = when {
    days == null -> ReminderStatus("Date not recognized", Color.Gray, Icons.Default.Schedule)
    days < 0 -> ReminderStatus("Expired ${-days} day${if (-days == 1) "" else "s"} ago", Color(0xFFFF5252), Icons.Default.Error)
    days == 0 -> ReminderStatus("Expires today", Color(0xFFFF5252), Icons.Default.Error)
    days <= 30 -> ReminderStatus("Expires in $days day${if (days == 1) "" else "s"}", Color(0xFFFFA726), Icons.Default.Schedule)
    else -> ReminderStatus("Valid for $days more days", Color(0xFF4CAF50), Icons.Default.CheckCircle)
}

@Composable
private fun ReminderCard(title: String, dateString: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(20.dp)
    ) {
        Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (dateString.isNullOrBlank()) {
            Text(text = "Not on file", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
        } else {
            val days = daysUntil(dateString)
            val status = statusFor(days)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(status.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = status.icon,
                        contentDescription = null,
                        tint = status.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = status.label, color = status.color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = dateString, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    AutomateTheme {
        NotificationsContent(
            licence = VehicleLicence(
                licencePlate = "AB-123-CD",
                licenceExpiryDate = "2026-09-01",
                inspectionExpiryDate = "2026-08-20"
            ),
            onBackClick = {},
            onAddLicenceClick = {}
        )
    }
}
