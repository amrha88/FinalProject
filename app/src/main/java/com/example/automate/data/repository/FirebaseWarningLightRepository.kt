package com.example.automate.data.repository

import android.graphics.Bitmap
import com.example.automate.domain.model.WarningLightAnalysis
import com.example.automate.domain.model.WarningLightResult
import com.example.automate.domain.model.WarningSeverity
import com.example.automate.domain.repository.WarningLightRepository
import com.example.automate.util.AiConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.util.UUID

class FirebaseWarningLightRepository : WarningLightRepository {
    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeWarningLight(bitmap: Bitmap): Result<WarningLightResult> {
        return try {
            val responseSchema = Schema.obj(
                properties = mapOf(
                    "warningLightDetected" to Schema.boolean(),
                    "warningName" to Schema.string(nullable = true),
                    "severity" to Schema.enumeration(
                        values = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL"),
                        nullable = true
                    ),
                    "confidence" to Schema.double(nullable = true),
                    "explanation" to Schema.string(nullable = true),
                    "recommendation" to Schema.string(nullable = true),
                    "canContinueDriving" to Schema.boolean(nullable = true),
                    "disclaimer" to Schema.string(nullable = true)
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
                        You analyze a photo of a car dashboard for an illuminated warning light.
                        Rules:
                        - Only describe a warning symbol that is clearly lit in the photo.
                        - Set warningLightDetected=false if no dashboard warning light icon is visible.
                        - Identify the specific warning (e.g. Check Engine, Oil Pressure, Battery, TPMS, ABS, Coolant Temperature).
                        - severity must be exactly one of: LOW, MEDIUM, HIGH, CRITICAL.
                        - confidence is a value between 0 and 1.
                        - explanation describes what the warning means.
                        - recommendation describes what the driver should do next.
                        - canContinueDriving reflects whether it is generally safe to keep driving with this warning lit.
                        - disclaimer must note this is an AI estimate and a professional mechanic should be consulted.
                        - Do not diagnose issues beyond what the lit icon indicates.
                        - Return only the structured schema. Do not return markdown or explanations outside the schema.
                    """.trimIndent())
                }
            )

            val prompt = content {
                image(bitmap)
                text("Identify the illuminated dashboard warning light in this photo.")
            }

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty AI response")
            val analysis = json.decodeFromString<WarningLightAnalysis>(responseText)

            if (!analysis.warningLightDetected) {
                return Result.failure(Exception("No dashboard warning light was detected in this photo."))
            }

            val severity = WarningSeverity.entries.find { it.name == analysis.severity } ?: WarningSeverity.UNKNOWN

            Result.success(
                WarningLightResult(
                    id = UUID.randomUUID().toString(),
                    warningName = analysis.warningName ?: "Unknown warning",
                    confidence = analysis.confidence ?: 0f,
                    severity = severity,
                    explanation = analysis.explanation ?: "No explanation available.",
                    recommendation = analysis.recommendation ?: "Consult a professional mechanic.",
                    canContinueDriving = analysis.canContinueDriving,
                    disclaimer = analysis.disclaimer
                        ?: "This is an AI estimate. Please consult a professional mechanic."
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
