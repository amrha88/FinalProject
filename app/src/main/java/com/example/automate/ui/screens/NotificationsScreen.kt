package com.example.automate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.domain.model.DatePrecision
import com.example.automate.domain.model.ReminderType
import com.example.automate.domain.model.VehicleReminder
import com.example.automate.ui.theme.AutomateTheme
import com.example.automate.ui.viewmodel.NotificationsUiState
import com.example.automate.ui.viewmodel.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun NotificationsScreen(
    vehicleId: String,
    viewModel: NotificationsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehicleId) {
        viewModel.loadReminders(vehicleId)
    }

    NotificationsContent(
        uiState = uiState,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsContent(
    uiState: NotificationsUiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF000C1F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF000C1F)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF007BFF))
                }
            } else if (uiState.reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No upcoming notifications", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(uiState.reminders) { reminder ->
                        ReminderCard(reminder = reminder)
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

enum class NotificationPriority(val label: String, val color: Color, val icon: ImageVector) {
    UPCOMING("Upcoming", Color(0xFF007BFF), Icons.Default.Timer),
    SOON("Soon", Color(0xFFFFB300), Icons.Default.Timer),
    DUE("Due Today", Color(0xFFF44336), Icons.Default.Warning),
    OVERDUE("Overdue", Color(0xFFB71C1C), Icons.Default.Warning)
}

@Composable
fun ReminderCard(reminder: VehicleReminder) {
    val daysLeft = reminder.dueDate?.let { daysUntil(it) }
    val priority = statusFor(daysLeft)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = priority.color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(priority.icon, contentDescription = null, tint = priority.color, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = reminder.title, fontWeight = FontWeight.Bold, color = Color(0xFF0B1730), fontSize = 16.sp)
                Text(
                    text = when {
                        reminder.datePrecision == DatePrecision.MONTH_ONLY -> "Expected in ${reminder.dueDate?.substring(0, 7) ?: ""}"
                        daysLeft == null -> "Pending"
                        daysLeft < 0 -> "Expired ${-daysLeft} days ago"
                        daysLeft == 0 -> "Due Today"
                        daysLeft == 1 -> "Tomorrow"
                        else -> "$daysLeft days remaining"
                    },
                    color = priority.color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun daysUntil(dateString: String): Int? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        val targetDate = sdf.parse(dateString) ?: return null
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val diff = targetDate.time - now.time
        TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
    } catch (e: Exception) {
        null
    }
}

private fun statusFor(daysLeft: Int?): NotificationPriority {
    return when {
        daysLeft == null -> NotificationPriority.UPCOMING
        daysLeft < 0 -> NotificationPriority.OVERDUE
        daysLeft == 0 -> NotificationPriority.DUE
        daysLeft <= 14 -> NotificationPriority.SOON
        else -> NotificationPriority.UPCOMING
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    AutomateTheme {
        NotificationsContent(
            uiState = NotificationsUiState(
                reminders = listOf(
                    VehicleReminder(type = ReminderType.VEHICLE_LICENCE, title = "Vehicle Licence", dueDate = "2026-12-20"),
                    VehicleReminder(type = ReminderType.INSURANCE, title = "Insurance", dueDate = "2024-03-10")
                )
            ),
            onBackClick = {}
        )
    }
}
