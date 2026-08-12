package com.example.automate.domain.engine

import com.example.automate.domain.model.*
import com.example.automate.domain.repository.ReminderRepository

class ReminderEngine(private val reminderRepository: ReminderRepository) {

    suspend fun syncRemindersFromDocument(vehicleId: String, document: VehicleDocument) {
        when (document.documentType) {
            VehicleDocumentType.VEHICLE_LICENCE -> {
                document.licenceExpiryDate?.let { expiry ->
                    updateReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.VEHICLE_LICENCE,
                        title = "Vehicle Licence",
                        description = "Licence renewal required",
                        dueDate = expiry,
                        sourceDocumentId = document.id
                    )
                }
            }
            VehicleDocumentType.INSPECTION -> {
                document.inspectionExpiryDate?.let { expiry ->
                    updateReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.VEHICLE_INSPECTION,
                        title = "Vehicle Inspection",
                        description = "Mandatory safety test",
                        dueDate = expiry,
                        sourceDocumentId = document.id
                    )
                }
            }
            VehicleDocumentType.INSURANCE -> {
                document.insuranceExpiryDate?.let { expiry ->
                    updateReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.INSURANCE,
                        title = "Insurance",
                        description = "Policy renewal",
                        dueDate = expiry,
                        sourceDocumentId = document.id
                    )
                }
            }
            VehicleDocumentType.MAINTENANCE, VehicleDocumentType.REPAIR_INVOICE -> {
                // If explicit next service info exists
                if (document.nextServiceDate != null || document.nextServiceMileage != null) {
                    updateReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.GENERAL_SERVICE,
                        title = "Scheduled Maintenance",
                        description = "Based on garage recommendation",
                        dueDate = document.nextServiceDate,
                        dueMileage = document.nextServiceMileage,
                        sourceDocumentId = document.id
                    )
                }
                
                // Specific item updates
                if (document.oilChanged == true && document.nextServiceDate != null) {
                     updateReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.ENGINE_OIL,
                        title = "Engine Oil",
                        dueDate = document.nextServiceDate,
                        sourceDocumentId = document.id
                    )
                }
            }
            else -> {}
        }
    }

    suspend fun syncRemindersFromHistory(vehicleId: String, event: VehicleHistoryEvent) {
        if (event.nextServiceDate != null || event.nextServiceMileage != null) {
            updateReminder(
                vehicleId = vehicleId,
                type = ReminderType.GENERAL_SERVICE,
                title = event.title,
                dueDate = event.nextServiceDate,
                dueMileage = event.nextServiceMileage,
                sourceHistoryEventId = event.id
            )
        }
        
        event.maintenanceItems.forEach { item ->
            if (item.nextDueDate != null || item.nextDueMileage != null) {
                updateReminder(
                    vehicleId = vehicleId,
                    type = mapMaintenanceToReminder(item.type),
                    title = item.type.name.replace("_", " "),
                    dueDate = item.nextDueDate,
                    dueMileage = item.nextDueMileage,
                    sourceHistoryEventId = event.id
                )
            }
        }
    }

    suspend fun syncRemindersFromManual(
        vehicleId: String,
        type: ReminderType,
        title: String,
        dueDate: String,
        precision: DatePrecision
    ) {
        updateReminder(
            vehicleId = vehicleId,
            type = type,
            title = title,
            dueDate = dueDate,
            datePrecision = precision
        )
    }

    private suspend fun updateReminder(
        vehicleId: String,
        type: ReminderType,
        title: String,
        description: String? = null,
        dueDate: String? = null,
        dueMileage: Int? = null,
        datePrecision: DatePrecision = DatePrecision.EXACT,
        sourceDocumentId: String? = null,
        sourceHistoryEventId: String? = null
    ) {
        // Deactivate old active reminders of same type for this vehicle
        reminderRepository.deactiveOldReminders(vehicleId, type)

        val reminder = VehicleReminder(
            vehicleId = vehicleId,
            type = type,
            title = title,
            description = description,
            dueDate = dueDate,
            dueMileage = dueMileage,
            datePrecision = datePrecision,
            sourceDocumentId = sourceDocumentId,
            sourceHistoryEventId = sourceHistoryEventId,
            status = ReminderStatus.ACTIVE
        )
        reminderRepository.saveReminder(reminder)
    }

    private fun mapMaintenanceToReminder(type: MaintenanceItemType): ReminderType {
        return try {
            ReminderType.valueOf(type.name)
        } catch (e: Exception) {
            ReminderType.OTHER
        }
    }
}
