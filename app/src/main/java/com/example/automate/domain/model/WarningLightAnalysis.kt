package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WarningLightAnalysis(
    val warningLightDetected: Boolean = false,
    val warningName: String? = null,
    val severity: String? = null,
    val confidence: Float? = null,
    val explanation: String? = null,
    val recommendation: String? = null,
    val canContinueDriving: Boolean? = null,
    val disclaimer: String? = null
)
