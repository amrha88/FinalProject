package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReminderStatus {
    ACTIVE,
    COMPLETED,
    REPLACED,
    DISMISSED
}
