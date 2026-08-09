package com.example.automate.domain.repository

import android.graphics.Bitmap
import com.example.automate.domain.model.WarningLightResult

interface WarningLightRepository {
    suspend fun analyzeWarningLight(bitmap: Bitmap): Result<WarningLightResult>
}
