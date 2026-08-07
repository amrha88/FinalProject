package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleLicenceExtraction(
    val ownerName: String? = null,
    val licencePlate: String? = null,
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
    val documentDetected: Boolean = false,
    val uncertainFields: List<String> = emptyList()
)
