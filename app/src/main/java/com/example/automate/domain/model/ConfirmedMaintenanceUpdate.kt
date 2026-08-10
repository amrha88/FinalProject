package com.example.automate.domain.model

data class ConfirmedMaintenanceUpdate(
    val eventDate: String,
    val mileage: Int?,
    val items: List<MaintenanceTextItem>,
    val garageName: String? = null,
    val notes: String? = null
)
