package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceExtraction(
    val serviceDate: String? = null,
    val mileage: Int? = null,
    val garageName: String? = null,
    val servicesPerformed: List<String> = emptyList(),
    val partsReplaced: List<String> = emptyList(),
    val engineOilChanged: Boolean? = null,
    val oilFilterChanged: Boolean? = null,
    val airFilterChanged: Boolean? = null,
    val cabinFilterChanged: Boolean? = null,
    val brakePadsChanged: Boolean? = null,
    val brakeDiscsChanged: Boolean? = null,
    val brakeFluidChanged: Boolean? = null,
    val coolantChanged: Boolean? = null,
    val sparkPlugsChanged: Boolean? = null,
    val batteryChanged: Boolean? = null,
    val tiresChanged: Boolean? = null,
    val timingBeltChanged: Boolean? = null,
    val timingChainServiced: Boolean? = null,
    val transmissionOilChanged: Boolean? = null,
    val fuelFilterChanged: Boolean? = null,
    val otherItems: List<String> = emptyList(),
    val totalAmount: Double? = null,
    val nextServiceDate: String? = null,
    val nextServiceMileage: Int? = null,
    val notes: String? = null,
    val uncertainFields: List<String> = emptyList(),
    val documentDetected: Boolean = false
)
