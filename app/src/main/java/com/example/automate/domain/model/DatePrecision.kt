package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DatePrecision {
    EXACT,
    MONTH_ONLY,
    UNKNOWN
}
