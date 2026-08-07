package com.example.automate.domain.repository

import com.example.automate.domain.model.VehicleLicence

interface VehicleLicenceRepository {
    suspend fun loadLicence(vehicleId: String): Result<VehicleLicence?>
    suspend fun saveLicence(vehicleId: String, licence: VehicleLicence): Result<Unit>
    suspend fun deleteLicence(vehicleId: String): Result<Unit>
}
