package com.example.automate.data.repository

import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleSpecs
import com.example.automate.domain.repository.VehicleSpecsRepository
import com.example.automate.util.AiConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class FirebaseVehicleSpecsRepository : VehicleSpecsRepository {
    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getSpecs(vehicle: Vehicle): Result<VehicleSpecs> {
        return try {
            val responseSchema = Schema.obj(
                properties = mapOf(
                    "fuelConsumptionL100km" to Schema.double(nullable = true),
                    "fuelType" to Schema.string(nullable = true),
                    "engineDisplacementL" to Schema.double(nullable = true),
                    "horsepower" to Schema.integer(nullable = true),
                    "transmission" to Schema.string(nullable = true),
                    "fuelTankCapacityL" to Schema.double(nullable = true)
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
                        You provide typical, representative specifications for a car model.
                        Rules:
                        - Give best-estimate typical values for the base/common trim of this manufacturer, model and year.
                        - fuelConsumptionL100km is the combined average fuel consumption in liters per 100km.
                        - Use null for any field you are not reasonably confident about.
                        - Do not fabricate precise figures; provide realistic, representative estimates.
                        - Return only the structured schema. Do not return markdown or explanations outside the schema.
                    """.trimIndent())
                }
            )

            val prompt = content {
                text("""
                    Manufacturer: ${vehicle.manufacturer}
                    Model: ${vehicle.model}
                    Year: ${vehicle.year}
                """.trimIndent())
            }

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty AI response")
            val specs = json.decodeFromString<VehicleSpecs>(responseText)

            Result.success(specs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
