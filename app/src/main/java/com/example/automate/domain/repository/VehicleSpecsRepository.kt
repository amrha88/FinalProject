package com.example.automate.domain.repository

import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleSpecs

interface VehicleSpecsRepository {
    suspend fun getSpecs(vehicle: Vehicle): Result<VehicleSpecs>
}
