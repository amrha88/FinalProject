package com.example.automate.domain.repository

import com.example.automate.domain.model.VehicleReminder

interface ReminderRepository {
    suspend fun getVehicleReminders(vehicleId: String): Result<List<VehicleReminder>>
    suspend fun getUserReminders(): Result<List<VehicleReminder>>
    suspend fun saveReminder(reminder: VehicleReminder): Result<String>
    suspend fun deactiveOldReminders(vehicleId: String, type: com.example.automate.domain.model.ReminderType): Result<Unit>
    suspend fun deleteReminder(vehicleId: String, reminderId: String): Result<Unit>
}
