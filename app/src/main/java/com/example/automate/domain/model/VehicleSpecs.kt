package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleSpecs(
    val fuelConsumptionL100km: Double? = null,
    val fuelType: String? = null,
    val engineDisplacementL: Double? = null,
    val horsepower: Int? = null,
    val transmission: String? = null,
    val fuelTankCapacityL: Double? = null
)
