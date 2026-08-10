package com.example.automate.domain.repository

import com.example.automate.domain.model.ConfirmedMaintenanceUpdate
import com.example.automate.domain.model.VehicleHistoryEvent
import com.example.automate.domain.model.VehicleMaintenanceItemState

interface VehicleHistoryRepository {
    suspend fun loadHistory(vehicleId: String): Result<List<VehicleHistoryEvent>>
    suspend fun saveHistoryEvent(vehicleId: String, event: VehicleHistoryEvent): Result<String>
    suspend fun updateHistoryEvent(vehicleId: String, event: VehicleHistoryEvent): Result<Unit>
    suspend fun deleteHistoryEvent(vehicleId: String, historyEventId: String): Result<Unit>
    suspend fun getHistoryEvent(vehicleId: String, historyEventId: String): Result<VehicleHistoryEvent>
    
    // New maintenance state methods
    suspend fun saveMaintenanceUpdate(vehicleId: String, update: ConfirmedMaintenanceUpdate): Result<Unit>
    suspend fun loadMaintenanceState(vehicleId: String): Result<List<VehicleMaintenanceItemState>>
}
