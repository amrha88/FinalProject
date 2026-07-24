package com.example.automate.domain.model

data class Vehicle(
    val id: String,
    val manufacturer: String,
    val model: String,
    val year: String,
    val plate: String,
    val isDark: Boolean = false
)
