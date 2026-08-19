package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleReminder(
    val id: String = "",
    val vehicleId: String = "", // empty for user-level reminders
    val type: ReminderType = ReminderType.OTHER,
    val title: String = "",
    val description: String? = null,
    val dueDate: String? = null,
    val dueMileage: Int? = null,
    val datePrecision: DatePrecision = DatePrecision.EXACT,
    val sourceDocumentId: String? = null,
    val sourceHistoryEventId: String? = null,
    val status: ReminderStatus = ReminderStatus.ACTIVE,
    val nextNotificationAt: Long? = null,
    val lastNotificationSentAt: Long? = null,
    val notificationStage: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
