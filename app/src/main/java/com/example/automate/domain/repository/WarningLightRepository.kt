package com.example.automate.domain.repository

import android.net.Uri
import com.example.automate.domain.model.WarningLightResult

interface WarningLightRepository {
    suspend fun analyzeWarningLight(imageUri: Uri): Result<WarningLightResult>
}
