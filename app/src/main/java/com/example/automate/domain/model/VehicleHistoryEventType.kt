package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class VehicleHistoryEventType {
    MAINTENANCE,
    REPAIR,
    OIL_CHANGE,
    INSPECTION,
    LICENCE_RENEWAL,
    INSURANCE_RENEWAL,
    DOCUMENT_UPDATE,
    WARNING_ANALYSIS,
    NOTIFICATION_SENT,
    MANUAL
}
