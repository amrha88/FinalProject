package com.example.automate.ui.viewmodel

data class VehicleUiModel(
    val id: String,
    val name: String,
    val year: String,
    val plate: String,
    val isDark: Boolean = false
)
