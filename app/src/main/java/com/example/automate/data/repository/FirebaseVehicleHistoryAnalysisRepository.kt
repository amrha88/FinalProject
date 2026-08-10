package com.example.automate.data.repository

import android.graphics.Bitmap
import com.example.automate.domain.model.*
import com.example.automate.domain.repository.VehicleHistoryAnalysisRepository
import com.example.automate.util.AiConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.json.Json

class FirebaseVehicleHistoryAnalysisRepository : VehicleHistoryAnalysisRepository {
    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeMaintenanceImage(
        vehicle: Vehicle,
        bitmap: Bitmap
    ): Result<MaintenanceExtraction> {
        return try {
            val responseSchema = Schema.obj(
                properties = mapOf(
                    "serviceDate" to Schema.string(nullable = true),
                    "mileage" to Schema.integer(nullable = true),
                    "garageName" to Schema.string(nullable = true),
                    "servicesPerformed" to Schema.array(Schema.string()),
                    "partsReplaced" to Schema.array(Schema.string()),
                    "engineOilChanged" to Schema.boolean(nullable = true),
                    "oilFilterChanged" to Schema.boolean(nullable = true),
                    "airFilterChanged" to Schema.boolean(nullable = true),
                    "cabinFilterChanged" to Schema.boolean(nullable = true),
                    "brakePadsChanged" to Schema.boolean(nullable = true),
                    "brakeDiscsChanged" to Schema.boolean(nullable = true),
                    "brakeFluidChanged" to Schema.boolean(nullable = true),
                    "coolantChanged" to Schema.boolean(nullable = true),
                    "sparkPlugsChanged" to Schema.boolean(nullable = true),
                    "batteryChanged" to Schema.boolean(nullable = true),
                    "tiresChanged" to Schema.boolean(nullable = true),
                    "timingBeltChanged" to Schema.boolean(nullable = true),
                    "timingChainServiced" to Schema.boolean(nullable = true),
                    "transmissionOilChanged" to Schema.boolean(nullable = true),
                    "fuelFilterChanged" to Schema.boolean(nullable = true),
                    "otherItems" to Schema.array(Schema.string()),
                    "totalAmount" to Schema.double(nullable = true),
                    "nextServiceDate" to Schema.string(nullable = true),
                    "nextServiceMileage" to Schema.integer(nullable = true),
                    "notes" to Schema.string(nullable = true),
                    "uncertainFields" to Schema.array(Schema.string()),
                    "documentDetected" to Schema.boolean()
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
                        You analyze vehicle maintenance and repair documents.
                        Extract only factual information that is clearly visible.
                        STRICT RULES:
                        - Never invent maintenance work.
                        - Never assume a component was replaced.
                        - Never calculate a service interval.
                        - Never create a next service date yourself.
                        - Never create a next service mileage yourself.
                        - Only return nextServiceDate or nextServiceMileage if explicitly written in the document.
                        - Never estimate current mileage.
                        - Do not confuse invoice number with mileage.
                        - Do not confuse invoice date with next-service date.
                        - Return null when information is missing or unreadable.
                        - Preserve numbers accurately.
                        - List each clearly identified replaced component.
                        - Add uncertain values to uncertainFields.
                        - Return structured JSON only.
                    """.trimIndent())
                }
            )

            val prompt = content {
                image(bitmap)
                text("""
                    Analyze this maintenance document for:
                    Manufacturer: ${vehicle.manufacturer}
                    Model: ${vehicle.model}
                    Year: ${vehicle.year}
                    Plate: ${vehicle.plate}
                """.trimIndent())
            }

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty AI response")
            val extraction = json.decodeFromString<MaintenanceExtraction>(responseText)
            
            Result.success(extraction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun analyzeQuickUpdateText(
        userInput: String,
        currentDate: String
    ): Result<MaintenanceTextExtraction> {
        return try {
            val responseSchema = Schema.obj(
                properties = mapOf(
                    "eventDate" to Schema.string(nullable = true),
                    "mileage" to Schema.integer(nullable = true),
                    "items" to Schema.array(
                        Schema.obj(
                            properties = mapOf(
                                "type" to Schema.enumeration(MaintenanceItemType.entries.map { it.name }),
                                "action" to Schema.enumeration(MaintenanceAction.entries.map { it.name })
                            )
                        )
                    ),
                    "garageName" to Schema.string(nullable = true),
                    "notes" to Schema.string(nullable = true),
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
                        You convert a user's description of vehicle maintenance into structured maintenance data.
                        STRICT RULES:
                        - Extract only what the user explicitly says.
                        - Never invent a maintenance item.
                        - Never invent mileage.
                        - Never invent a date.
                        - If the user says 'today', use the current local date provided: $currentDate
                        - If the user says 'yesterday', calculate it relative to: $currentDate
                        - Do not calculate maintenance intervals.
                        - Do not generate next service dates.
                        - Do not generate next service mileage.
                        - If an item is unclear, add it to uncertainFields.
                        - Return structured JSON only.
                    """.trimIndent())
                }
            )

            val response = generativeModel.generateContent(userInput)
            val responseText = response.text ?: throw Exception("Empty AI response")
            val extraction = json.decodeFromString<MaintenanceTextExtraction>(responseText)
            
            Result.success(extraction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
