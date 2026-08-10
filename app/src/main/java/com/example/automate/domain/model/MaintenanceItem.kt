package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceItem(
    val type: MaintenanceItemType,
    val action: String? = null,
    val confirmed: Boolean = true,
    val nextDueDate: String? = null,
    val nextDueMileage: Int? = null
)
