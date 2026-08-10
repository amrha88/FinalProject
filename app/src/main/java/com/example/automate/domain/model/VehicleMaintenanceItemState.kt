package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleMaintenanceItemState(
    val type: MaintenanceItemType,
    val lastAction: MaintenanceAction? = null,
    val lastServiceDate: String? = null,
    val lastServiceMileage: Int? = null,
    val nextDueDate: String? = null,
    val nextDueMileage: Int? = null,
    val sourceHistoryEventId: String? = null,
    val updatedAt: Long = 0L
)
