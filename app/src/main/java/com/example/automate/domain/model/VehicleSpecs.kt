package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EngineVariant(
    val name: String? = null,
    val fuelConsumptionL100km: Double? = null,
    val fuelType: String? = null,
    val engineDisplacementL: Double? = null,
    val horsepower: Int? = null,
    val transmission: String? = null,
    val fuelTankCapacityL: Double? = null
)

@Serializable
data class VehicleSpecs(
    val fuelConsumptionL100km: Double? = null,
    val fuelType: String? = null,
    val engineDisplacementL: Double? = null,
    val horsepower: Int? = null,
    val transmission: String? = null,
    val fuelTankCapacityL: Double? = null,
    val variants: List<EngineVariant> = emptyList(),
    val selectedVariantName: String? = null
) {
    fun withSelectedVariant(variant: EngineVariant): VehicleSpecs = copy(
        fuelConsumptionL100km = variant.fuelConsumptionL100km,
        fuelType = variant.fuelType,
        engineDisplacementL = variant.engineDisplacementL,
        horsepower = variant.horsepower,
        transmission = variant.transmission,
        fuelTankCapacityL = variant.fuelTankCapacityL,
        selectedVariantName = variant.name
    )
}
