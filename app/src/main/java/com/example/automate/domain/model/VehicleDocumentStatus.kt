package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class VehicleDocumentStatus {
    ACTIVE,
    REPLACED,
    ARCHIVED
}
