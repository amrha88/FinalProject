package com.example.automate.domain.repository

import com.example.automate.domain.model.Vehicle

interface VehicleRepository {
    suspend fun getVehicles(): Result<List<Vehicle>>
    suspend fun addVehicle(vehicle: Vehicle): Result<Unit>
}