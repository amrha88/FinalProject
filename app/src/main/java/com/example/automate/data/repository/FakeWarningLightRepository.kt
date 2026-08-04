package com.example.automate.data.repository

import android.net.Uri
import com.example.automate.domain.model.WarningLightResult
import com.example.automate.domain.model.WarningSeverity
import com.example.automate.domain.repository.WarningLightRepository
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

class FakeWarningLightRepository : WarningLightRepository {

    private val sampleResults = listOf(
        WarningLightResult(
            id = UUID.randomUUID().toString(),
            warningName = "Check Engine",
            confidence = 0.95f,
            severity = WarningSeverity.HIGH,
            explanation = "The engine control unit has detected a malfunction in the engine system or emission control.",
            recommendation = "Schedule an appointment with a mechanic as soon as possible for a diagnostic scan.",
            canContinueDriving = true,
            disclaimer = "This is an AI estimate. Please consult a professional mechanic."
        ),
        WarningLightResult(
            id = UUID.randomUUID().toString(),
            warningName = "Oil Pressure",
            confidence = 0.98f,
            severity = WarningSeverity.CRITICAL,
            explanation = "Low oil pressure detected. This can lead to severe engine damage.",
            recommendation = "Stop the vehicle safely immediately. Check oil levels and do not drive until the issue is resolved.",
            canContinueDriving = false,
            disclaimer = "This is an AI estimate. Stop driving immediately if this light is red."
        ),
        WarningLightResult(
            id = UUID.randomUUID().toString(),
            warningName = "Battery/Charging System",
            confidence = 0.92f,
            severity = WarningSeverity.MEDIUM,
            explanation = "The battery is not charging properly or there is a fault in the alternator.",
            recommendation = "Turn off unnecessary electrical components and drive to a service station immediately.",
            canContinueDriving = true,
            disclaimer = "This is an AI estimate. Your car may fail to start if the battery drains."
        ),
        WarningLightResult(
            id = UUID.randomUUID().toString(),
            warningName = "Tire Pressure (TPMS)",
            confidence = 0.97f,
            severity = WarningSeverity.LOW,
            explanation = "One or more tires have low air pressure.",
            recommendation = "Check all tire pressures and inflate to the recommended level.",
            canContinueDriving = true,
            disclaimer = "This is an AI estimate. Correct tire pressure is essential for safety."
        ),
        WarningLightResult(
            id = UUID.randomUUID().toString(),
            warningName = "ABS Warning",
            confidence = 0.94f,
            severity = WarningSeverity.MEDIUM,
            explanation = "A fault has been detected in the Anti-lock Braking System.",
            recommendation = "Normal braking is still functional, but ABS may not work. Have it checked soon.",
            canContinueDriving = true,
            disclaimer = "This is an AI estimate. Avoid hard braking until inspected."
        ),
        WarningLightResult(
            id = UUID.randomUUID().toString(),
            warningName = "Coolant Temperature",
            confidence = 0.99f,
            severity = WarningSeverity.CRITICAL,
            explanation = "The engine is overheating.",
            recommendation = "Pull over and turn off the engine immediately. Wait for it to cool before checking levels.",
            canContinueDriving = false,
            disclaimer = "This is an AI estimate. Overheating can destroy your engine."
        )
    )

    override suspend fun analyzeWarningLight(imageUri: Uri): Result<WarningLightResult> {
        delay(2000) // Simulate network/AI delay
        return try {
            val result = sampleResults[Random.nextInt(sampleResults.size)]
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
