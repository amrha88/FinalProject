package com.example.automate.domain.engine

import com.example.automate.domain.model.*
import com.example.automate.domain.repository.ReminderRepository
import java.util.Locale

class ReminderEngine(private val reminderRepository: ReminderRepository) {

    fun buildRemindersFromDocument(vehicleId: String, document: VehicleDocument): List<VehicleReminder> {
        val reminders = mutableListOf<VehicleReminder>()
        when (document.documentType) {
            VehicleDocumentType.VEHICLE_LICENCE -> {
                document.licenceExpiryDate?.let { expiry ->
                    reminders.add(createReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.VEHICLE_LICENCE,
                        title = "Vehicle Licence",
                        description = "Licence renewal required",
                        dueDate = expiry
                    ))
                }
            }
            VehicleDocumentType.INSPECTION -> {
                document.inspectionExpiryDate?.let { expiry ->
                    reminders.add(createReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.VEHICLE_INSPECTION,
                        title = "Vehicle Inspection",
                        description = "Mandatory safety test",
                        dueDate = expiry
                    ))
                }
            }
            VehicleDocumentType.INSURANCE -> {
                document.insuranceExpiryDate?.let { expiry ->
                    reminders.add(createReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.INSURANCE,
                        title = "Insurance",
                        description = "Policy renewal",
                        dueDate = expiry
                    ))
                }
            }
            VehicleDocumentType.MAINTENANCE, VehicleDocumentType.REPAIR_INVOICE -> {
                if (document.nextServiceDate != null || document.nextServiceMileage != null) {
                    reminders.add(createReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.GENERAL_SERVICE,
                        title = "Scheduled Maintenance",
                        description = "Based on garage recommendation",
                        dueDate = document.nextServiceDate,
                        dueMileage = document.nextServiceMileage
                    ))
                }
                
                if (document.oilChanged == true && document.nextServiceDate != null) {
                     reminders.add(createReminder(
                        vehicleId = vehicleId,
                        type = ReminderType.ENGINE_OIL,
                        title = "Engine Oil",
                        dueDate = document.nextServiceDate
                    ))
                }
            }
            else -> {}
        }
        return reminders
    }

    fun buildHistoryEventFromDocument(vehicleId: String, doc: VehicleDocument): VehicleHistoryEvent? {
        if (doc.documentType != VehicleDocumentType.MAINTENANCE && 
            doc.documentType != VehicleDocumentType.REPAIR_INVOICE &&
            doc.documentType != VehicleDocumentType.INSPECTION &&
            doc.documentType != VehicleDocumentType.VEHICLE_LICENCE &&
            doc.documentType != VehicleDocumentType.INSURANCE) {
            return null
        }

        val type = when (doc.documentType) {
            VehicleDocumentType.REPAIR_INVOICE -> VehicleHistoryEventType.REPAIR
            VehicleDocumentType.MAINTENANCE -> VehicleHistoryEventType.MAINTENANCE
            VehicleDocumentType.INSPECTION -> VehicleHistoryEventType.INSPECTION
            VehicleDocumentType.VEHICLE_LICENCE -> VehicleHistoryEventType.LICENCE_RENEWAL
            VehicleDocumentType.INSURANCE -> VehicleHistoryEventType.INSURANCE_RENEWAL
            else -> VehicleHistoryEventType.MAINTENANCE
        }

        val title = when (doc.documentType) {
            VehicleDocumentType.VEHICLE_LICENCE -> "Vehicle licence renewed"
            VehicleDocumentType.INSURANCE -> "Insurance renewed"
            VehicleDocumentType.INSPECTION -> "Vehicle inspection completed"
            else -> doc.documentTitle ?: "Service Record"
        }

        val maintenanceItems = mutableListOf<MaintenanceItem>()
        if (doc.oilChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.ENGINE_OIL, "Replaced"))
        if (doc.oilFilterChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.OIL_FILTER, "Replaced"))
        if (doc.airFilterChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.AIR_FILTER, "Replaced"))
        if (doc.cabinFilterChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.CABIN_FILTER, "Replaced"))
        if (doc.brakePadsChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.BRAKE_PADS, "Replaced"))
        if (doc.batteryChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.BATTERY, "Replaced"))
        if (doc.tiresChanged == true) maintenanceItems.add(MaintenanceItem(MaintenanceItemType.TIRES, "Replaced"))

        return VehicleHistoryEvent(
            vehicleId = vehicleId,
            type = type,
            title = title,
            description = doc.summary,
            eventDate = doc.documentDate,
            mileage = doc.mileage,
            garageName = doc.garageOrProvider,
            servicesPerformed = doc.serviceItems,
            maintenanceItems = maintenanceItems,
            nextServiceDate = doc.nextServiceDate,
            nextServiceMileage = doc.nextServiceMileage,
            confirmedByUser = true
        )
    }

    fun buildRemindersFromHistory(vehicleId: String, event: VehicleHistoryEvent): List<VehicleReminder> {
        val reminders = mutableListOf<VehicleReminder>()
        if (event.nextServiceDate != null || event.nextServiceMileage != null) {
            reminders.add(createReminder(
                vehicleId = vehicleId,
                type = ReminderType.GENERAL_SERVICE,
                title = event.title,
                dueDate = event.nextServiceDate,
                dueMileage = event.nextServiceMileage
            ))
        }
        
        event.maintenanceItems.forEach { item ->
            if (item.nextDueDate != null || item.nextDueMileage != null) {
                reminders.add(createReminder(
                    vehicleId = vehicleId,
                    type = mapMaintenanceToReminder(item.type),
                    title = item.type.name.replace("_", " ").lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                    dueDate = item.nextDueDate,
                    dueMileage = item.nextDueMileage
                ))
            }
        }
        return reminders
    }

    suspend fun syncRemindersFromManual(
        vehicleId: String,
        type: ReminderType,
        title: String,
        dueDate: String,
        precision: DatePrecision
    ) {
        // Deactivate old active reminders of same type for this vehicle
        reminderRepository.deactiveOldReminders(vehicleId, type)

        val reminder = createReminder(
            vehicleId = vehicleId,
            type = type,
            title = title,
            dueDate = dueDate,
            datePrecision = precision
        )
        reminderRepository.saveReminder(reminder)
    }

    private fun createReminder(
        vehicleId: String,
        type: ReminderType,
        title: String,
        description: String? = null,
        dueDate: String? = null,
        dueMileage: Int? = null,
        datePrecision: DatePrecision = DatePrecision.EXACT
    ): VehicleReminder {
        return VehicleReminder(
            vehicleId = vehicleId,
            type = type,
            title = title,
            description = description,
            dueDate = dueDate,
            dueMileage = dueMileage,
            datePrecision = datePrecision,
            status = ReminderStatus.ACTIVE
        )
    }

    private fun mapMaintenanceToReminder(type: MaintenanceItemType): ReminderType {
        return try {
            ReminderType.valueOf(type.name)
        } catch (e: Exception) {
            ReminderType.OTHER
        }
    }
}
