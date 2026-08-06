package com.example.automate.ui.viewmodel

data class VehicleUiModel(
    val id: String,
    val manufacturer: String,
    val model: String,
    val year: String,
    val plate: String,
    val isDark: Boolean = false,
    val photoBase64: String? = null
)
