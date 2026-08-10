package com.example.automate.domain.repository

import android.graphics.Bitmap
import com.example.automate.domain.model.MaintenanceExtraction
import com.example.automate.domain.model.MaintenanceTextExtraction
import com.example.automate.domain.model.Vehicle

interface VehicleHistoryAnalysisRepository {
    suspend fun analyzeMaintenanceImage(
        vehicle: Vehicle,
        bitmap: Bitmap
    ): Result<MaintenanceExtraction>

    suspend fun analyzeQuickUpdateText(
        userInput: String,
        currentDate: String
    ): Result<MaintenanceTextExtraction>
}
