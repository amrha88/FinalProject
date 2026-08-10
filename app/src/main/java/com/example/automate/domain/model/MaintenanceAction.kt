package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MaintenanceAction {
    REPLACED,
    SERVICED,
    CHECKED
}
