package com.example.automate.domain.repository

import android.graphics.Bitmap
import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleLicenceExtraction

interface VehicleLicenceAnalysisRepository {
    suspend fun analyzeLicenceImage(
        vehicle: Vehicle,
        bitmap: Bitmap
    ): Result<VehicleLicenceExtraction>
}
