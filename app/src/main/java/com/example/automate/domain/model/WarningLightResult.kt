package com.example.automate.domain.model

data class WarningLightResult(
    val id: String,
    val warningName: String,
    val confidence: Float,
    val severity: WarningSeverity,
    val explanation: String,
    val recommendation: String,
    val canContinueDriving: Boolean?,
    val disclaimer: String
)
