package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleHistoryEvent(
    val id: String = "",
    val vehicleId: String = "",
    val type: VehicleHistoryEventType,
    val title: String = "",
    val description: String? = null,
    val eventDate: String? = null,
    val mileage: Int? = null,
    val garageName: String? = null,
    val servicesPerformed: List<String> = emptyList(),
    val partsReplaced: List<String> = emptyList(),
    val maintenanceItems: List<MaintenanceItem> = emptyList(),
    val nextServiceDate: String? = null,
    val nextServiceMileage: Int? = null,
    val totalAmount: Double? = null,
    val sourceDocumentId: String? = null,
    val sourceNotificationId: String? = null,
    val confirmedByUser: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
