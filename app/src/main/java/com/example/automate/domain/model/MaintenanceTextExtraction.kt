package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceTextExtraction(
    val eventDate: String? = null,
    val mileage: Int? = null,
    val items: List<MaintenanceTextItem> = emptyList(),
    val garageName: String? = null,
    val notes: String? = null,
    val uncertainFields: List<String> = emptyList()
)

@Serializable
data class MaintenanceTextItem(
    val type: MaintenanceItemType,
    val action: MaintenanceAction
)
