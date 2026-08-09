package com.example.automate.data.repository

import android.graphics.Bitmap
import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleDocumentExtraction
import com.example.automate.domain.repository.VehicleDocumentAnalysisRepository
import com.example.automate.util.AiConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.json.Json

class FirebaseVehicleDocumentAnalysisRepository : VehicleDocumentAnalysisRepository {
    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeDocument(
        vehicle: Vehicle,
        bitmap: Bitmap
    ): Result<VehicleDocumentExtraction> {
        return try {
            val responseSchema = Schema.obj(
                properties = mapOf(
                    "documentType" to Schema.enumeration(
                        values = listOf("VEHICLE_LICENCE", "MAINTENANCE", "INSPECTION", "INSURANCE", "REPAIR_INVOICE", "OTHER")
                    ),
                    "documentTitle" to Schema.string(nullable = true),
                    "documentDate" to Schema.string(nullable = true),
                    "vehiclePlate" to Schema.string(nullable = true),
                    "mileage" to Schema.integer(nullable = true),
                    "garageOrProvider" to Schema.string(nullable = true),
                    "serviceItems" to Schema.array(Schema.string()),
                    "oilChanged" to Schema.boolean(nullable = true),
                    "oilFilterChanged" to Schema.boolean(nullable = true),
                    "airFilterChanged" to Schema.boolean(nullable = true),
                    "cabinFilterChanged" to Schema.boolean(nullable = true),
                    "brakeFluidChanged" to Schema.boolean(nullable = true),
                    "coolantChanged" to Schema.boolean(nullable = true),
                    "sparkPlugsChanged" to Schema.boolean(nullable = true),
                    "brakePadsChanged" to Schema.boolean(nullable = true),
                    "batteryChanged" to Schema.boolean(nullable = true),
                    "tiresChanged" to Schema.boolean(nullable = true),
                    "timingBeltChanged" to Schema.boolean(nullable = true),
                    "licenceExpiryDate" to Schema.string(nullable = true),
                    "inspectionDate" to Schema.string(nullable = true),
                    "inspectionExpiryDate" to Schema.string(nullable = true),
                    "insuranceProvider" to Schema.string(nullable = true),
                    "policyNumber" to Schema.string(nullable = true),
                    "insuranceStartDate" to Schema.string(nullable = true),
                    "insuranceExpiryDate" to Schema.string(nullable = true),
                    "repairsPerformed" to Schema.array(Schema.string()),
                    "partsReplaced" to Schema.array(Schema.string()),
                    "nextServiceDate" to Schema.string(nullable = true),
                    "nextServiceMileage" to Schema.integer(nullable = true),
                    "summary" to Schema.string(nullable = true),
                    "documentDetected" to Schema.boolean(),
                    "uncertainFields" to Schema.array(Schema.string())
                )
            )

            val generativeModel = ai.generativeModel(
                modelName = AiConfig.MODEL_NAME,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    this.responseSchema = responseSchema
                },
                systemInstruction = content {
                    text("""
                        You are analyzing documents belonging to a vehicle-management application.
                        Your task is to identify the document type and extract structured factual information.
                        STRICT RULES:
                        - Extract only information visible in the document.
                        - Never invent missing information.
                        - Never estimate missing dates.
                        - Never estimate mileage.
                        - Never assume a maintenance item was replaced unless the document indicates it.
                        - Return null for unreadable or unavailable fields.
                        - Preserve numbers accurately.
                        - Distinguish between service date and next service date.
                        - Distinguish between current mileage and next service mileage.
                        - Distinguish between document issue date and expiry date.
                        - Do not treat invoice date as next service date.
                        - Do not calculate maintenance schedules yourself.
                        - Do not generate reminder dates.
                        - Do not provide mechanical advice.
                        - Do not save data automatically.
                        - Return structured JSON only according to the required schema.
                    """.trimIndent())
                }
            )

            val prompt = content {
                image(bitmap)
                text("""
                    Analyze this document for the following vehicle:
                    Manufacturer: ${vehicle.manufacturer}
                    Model: ${vehicle.model}
                    Year: ${vehicle.year}
                    Existing Plate: ${vehicle.plate}
                """.trimIndent())
            }

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty AI response")
            val extraction = json.decodeFromString<VehicleDocumentExtraction>(responseText)
            
            Result.success(extraction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
