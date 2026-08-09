package com.example.automate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleDocumentExtraction(
    val documentType: VehicleDocumentType,
    val documentTitle: String? = null,
    val documentDate: String? = null,

    val vehiclePlate: String? = null,
    val mileage: Int? = null,

    val garageOrProvider: String? = null,

    val serviceItems: List<String> = emptyList(),

    val oilChanged: Boolean? = null,
    val oilFilterChanged: Boolean? = null,
    val airFilterChanged: Boolean? = null,
    val cabinFilterChanged: Boolean? = null,
    val brakeFluidChanged: Boolean? = null,
    val coolantChanged: Boolean? = null,
    val sparkPlugsChanged: Boolean? = null,
    val brakePadsChanged: Boolean? = null,
    val batteryChanged: Boolean? = null,
    val tiresChanged: Boolean? = null,
    val timingBeltChanged: Boolean? = null,

    val licenceExpiryDate: String? = null,
    val inspectionDate: String? = null,
    val inspectionExpiryDate: String? = null,

    val insuranceProvider: String? = null,
    val policyNumber: String? = null,
    val insuranceStartDate: String? = null,
    val insuranceExpiryDate: String? = null,

    val repairsPerformed: List<String> = emptyList(),
    val partsReplaced: List<String> = emptyList(),

    val nextServiceDate: String? = null,
    val nextServiceMileage: Int? = null,

    val summary: String? = null,

    val documentDetected: Boolean = false,
    val uncertainFields: List<String> = emptyList()
)
