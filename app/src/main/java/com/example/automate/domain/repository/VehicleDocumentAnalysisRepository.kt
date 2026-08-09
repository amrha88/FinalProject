package com.example.automate.domain.repository

import android.graphics.Bitmap
import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleDocumentExtraction

interface VehicleDocumentAnalysisRepository {
    suspend fun analyzeDocument(
        vehicle: Vehicle,
        bitmap: Bitmap
    ): Result<VehicleDocumentExtraction>
}
