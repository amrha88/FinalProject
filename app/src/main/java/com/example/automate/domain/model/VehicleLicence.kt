package com.example.automate.domain.model

data class VehicleLicence(
    val id: String = "",
    val vehicleId: String = "",
    val ownerName: String? = null,
    val licencePlate: String = "",
    val manufacturer: String? = null,
    val model: String? = null,
    val modelCode: String? = null,
    val year: String? = null,
    val color: String? = null,
    val chassisNumber: String? = null,
    val engineNumber: String? = null,
    val ownershipDate: String? = null,
    val registrationDate: String? = null,
    val licenceExpiryDate: String? = null,
    val inspectionExpiryDate: String? = null,
    val fuelType: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
