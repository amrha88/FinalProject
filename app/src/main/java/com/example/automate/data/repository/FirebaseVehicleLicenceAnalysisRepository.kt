package com.example.automate.data.repository

import android.graphics.Bitmap
import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleLicenceExtraction
import com.example.automate.domain.repository.VehicleLicenceAnalysisRepository
import com.example.automate.util.AiConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class FirebaseVehicleLicenceAnalysisRepository : VehicleLicenceAnalysisRepository {
    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeLicenceImage(
        vehicle: Vehicle,
        bitmap: Bitmap
    ): Result<VehicleLicenceExtraction> {
        return try {
            val responseSchema = Schema.obj(
                properties = mapOf(
                    "ownerName" to Schema.string(nullable = true),
                    "licencePlate" to Schema.string(nullable = true),
                    "manufacturer" to Schema.string(nullable = true),
                    "model" to Schema.string(nullable = true),
                    "modelCode" to Schema.string(nullable = true),
                    "year" to Schema.string(nullable = true),
                    "color" to Schema.string(nullable = true),
                    "chassisNumber" to Schema.string(nullable = true),
                    "engineNumber" to Schema.string(nullable = true),
                    "ownershipDate" to Schema.string(nullable = true),
                    "registrationDate" to Schema.string(nullable = true),
                    "licenceExpiryDate" to Schema.string(nullable = true),
                    "inspectionExpiryDate" to Schema.string(nullable = true),
                    "fuelType" to Schema.string(nullable = true),
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
                        You analyze a vehicle registration/licence document.
                        Extract only values clearly visible in the image.
                        Rules:
                        - Do not guess blurry or missing values.
                        - Return null or an empty value when unreadable.
                        - Do not calculate dates.
                        - Do not infer values from general vehicle knowledge.
                        - Preserve visible letters and digits exactly where possible.
                        - Do not confuse a document number with the licence plate.
                        - Return dates in one consistent format (YYYY-MM-DD).
                        - Set documentDetected=false if the image is not a vehicle licence.
                        - Add unclear fields to uncertainFields.
                        - Return only the structured schema.
                        - Do not return markdown or explanations.
                    """.trimIndent())
                }
            )

            val prompt = content {
                image(bitmap)
                text("""
                    Analyze this vehicle licence for the following vehicle:
                    Manufacturer: ${vehicle.manufacturer}
                    Model: ${vehicle.model}
                    Year: ${vehicle.year}
                    Existing Plate: ${vehicle.plate}
                """.trimIndent())
            }

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty AI response")
            val extraction = json.decodeFromString<VehicleLicenceExtraction>(responseText)
            
            Result.success(extraction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
