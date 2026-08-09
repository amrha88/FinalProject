package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class VehicleDocumentType {
    VEHICLE_LICENCE,
    MAINTENANCE,
    INSPECTION,
    INSURANCE,
    REPAIR_INVOICE,
    OTHER
}
