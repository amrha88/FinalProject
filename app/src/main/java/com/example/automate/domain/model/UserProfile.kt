package com.example.automate.domain.model

data class UserProfile(
    val fullName: String,
    val age: Int,
    val email: String,
    val hasLicense: Boolean,
    val photoBase64: String? = null
)