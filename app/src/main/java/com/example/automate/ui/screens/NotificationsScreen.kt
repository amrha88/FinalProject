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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.R
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
    currentMileage: Int? = null,
    viewModel: NotificationsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehicleId) {
        viewModel.loadReminders(vehicleId)
    }

    NotificationsContent(
        uiState = uiState,
        currentMileage = currentMileage,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsContent(
    uiState: NotificationsUiState,
    currentMileage: Int? = null,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.notifications_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
                        Text(stringResource(R.string.notifications_empty), color = Color.Gray)
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
                        ReminderCard(reminder = reminder, currentMileage = currentMileage)
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
fun ReminderCard(reminder: VehicleReminder, currentMileage: Int? = null) {
    val context = LocalContext.current
    val daysLeft = reminder.dueDate?.let { daysUntil(it) }
    val mileageLeft = if (reminder.dueMileage != null && currentMileage != null) reminder.dueMileage - currentMileage else null

    val priority = statusFor(daysLeft, mileageLeft)

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

                val statusText = buildString {
                    if (daysLeft != null) {
                        append(
                            when {
                                reminder.datePrecision == DatePrecision.MONTH_ONLY ->
                                    context.getString(R.string.reminder_expected_in, reminder.dueDate.substring(0, 7))
                                daysLeft < 0 -> context.getString(R.string.reminder_expired_days_ago, -daysLeft)
                                daysLeft == 0 -> context.getString(R.string.reminder_due_today)
                                daysLeft == 1 -> context.getString(R.string.reminder_tomorrow)
                                else -> context.getString(R.string.reminder_days_remaining, daysLeft)
                            }
                        )
                    }

                    if (mileageLeft != null) {
                        if (isNotEmpty()) append(" • ")
                        append(
                            when {
                                mileageLeft < 0 -> context.getString(R.string.reminder_overdue_by_km, -mileageLeft)
                                mileageLeft == 0 -> context.getString(R.string.reminder_due_now)
                                else -> context.getString(R.string.reminder_km_remaining, mileageLeft)
                            }
                        )
                    }

                    if (isEmpty()) append(context.getString(R.string.reminder_pending))
                }

                Text(
                    text = statusText,
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

private fun statusFor(daysLeft: Int?, mileageLeft: Int?): NotificationPriority {
    val isOverdue = (daysLeft != null && daysLeft < 0) || (mileageLeft != null && mileageLeft < 0)
    val isDue = (daysLeft != null && daysLeft == 0) || (mileageLeft != null && mileageLeft == 0)
    val isSoon = (daysLeft != null && daysLeft <= 14) || (mileageLeft != null && mileageLeft <= 500)

    return when {
        isOverdue -> NotificationPriority.OVERDUE
        isDue -> NotificationPriority.DUE
        isSoon -> NotificationPriority.SOON
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
