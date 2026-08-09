package com.example.automate.domain.repository

import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleSpecs

interface VehicleRepository {
    suspend fun getVehicles(): Result<List<Vehicle>>
    suspend fun addVehicle(vehicle: Vehicle): Result<Unit>
    suspend fun updateVehicle(vehicle: Vehicle): Result<Unit>
    suspend fun deleteVehicle(vehicleId: String): Result<Unit>
    suspend fun saveSpecs(vehicleId: String, specs: VehicleSpecs): Result<Unit>
}