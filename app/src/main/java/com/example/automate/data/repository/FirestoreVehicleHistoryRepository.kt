package com.example.automate.data.repository

import com.example.automate.domain.model.*
import com.example.automate.domain.repository.VehicleHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirestoreVehicleHistoryRepository : VehicleHistoryRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getHistoryCollection(vehicleId: String) = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vehicles")
            .document(vehicleId)
            .collection("history")
    }

    private fun getMaintenanceStateCollection(vehicleId: String) = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vehicles")
            .document(vehicleId)
            .collection("maintenanceState")
    }

    override suspend fun loadHistory(vehicleId: String): Result<List<VehicleHistoryEvent>> {
        return try {
            val collection = getHistoryCollection(vehicleId) ?: throw Exception("User not authenticated")
            val snapshot = collection.orderBy("eventDate", Query.Direction.DESCENDING).get().await()
            val events = snapshot.toObjects(VehicleHistoryEvent::class.java)
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveHistoryEvent(vehicleId: String, event: VehicleHistoryEvent): Result<String> {
        return try {
            val collection = getHistoryCollection(vehicleId) ?: throw Exception("User not authenticated")
            val newDocRef = collection.document()
            val data = event.copy(
                id = newDocRef.id,
                vehicleId = vehicleId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            newDocRef.set(data).await()
            Result.success(newDocRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateHistoryEvent(vehicleId: String, event: VehicleHistoryEvent): Result<Unit> {
        return try {
            val collection = getHistoryCollection(vehicleId) ?: throw Exception("User not authenticated")
            val data = event.copy(updatedAt = System.currentTimeMillis())
            collection.document(event.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteHistoryEvent(vehicleId: String, historyEventId: String): Result<Unit> {
        return try {
            val collection = getHistoryCollection(vehicleId) ?: throw Exception("User not authenticated")
            collection.document(historyEventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHistoryEvent(vehicleId: String, historyEventId: String): Result<VehicleHistoryEvent> {
        return try {
            val collection = getHistoryCollection(vehicleId) ?: throw Exception("User not authenticated")
            val snapshot = collection.document(historyEventId).get().await()
            val event = snapshot.toObject(VehicleHistoryEvent::class.java) ?: throw Exception("Event not found")
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveMaintenanceUpdate(vehicleId: String, update: ConfirmedMaintenanceUpdate): Result<Unit> {
        return try {
            val historyCollection = getHistoryCollection(vehicleId) ?: throw Exception("User not authenticated")
            val stateCollection = getMaintenanceStateCollection(vehicleId) ?: throw Exception("User not authenticated")
            
            firestore.runTransaction { transaction ->
                // 1. Create History Event
                val newEventRef = historyCollection.document()
                val eventTitle = if (update.items.size == 1) {
                    "${update.items[0].type.name.replace("_", " ").lowercase().capitalize()} ${update.items[0].action.name.lowercase()}"
                } else {
                    "Maintenance Service"
                }
                
                val maintenanceItems = update.items.map { 
                    MaintenanceItem(type = it.type, action = it.action.name)
                }

                val event = VehicleHistoryEvent(
                    id = newEventRef.id,
                    vehicleId = vehicleId,
                    type = VehicleHistoryEventType.MAINTENANCE,
                    title = eventTitle,
                    description = update.notes,
                    eventDate = update.eventDate,
                    mileage = update.mileage,
                    garageName = update.garageName,
                    maintenanceItems = maintenanceItems,
                    confirmedByUser = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                transaction.set(newEventRef, event)

                // 2. Update Maintenance States
                update.items.forEach { item ->
                    val stateRef = stateCollection.document(item.type.name)
                    val newState = VehicleMaintenanceItemState(
                        type = item.type,
                        lastAction = item.action,
                        lastServiceDate = update.eventDate,
                        lastServiceMileage = update.mileage,
                        sourceHistoryEventId = newEventRef.id,
                        updatedAt = System.currentTimeMillis()
                    )
                    transaction.set(stateRef, newState)
                }
                null
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadMaintenanceState(vehicleId: String): Result<List<VehicleMaintenanceItemState>> {
        return try {
            val collection = getMaintenanceStateCollection(vehicleId) ?: throw Exception("User not authenticated")
            val snapshot = collection.get().await()
            val states = snapshot.toObjects(VehicleMaintenanceItemState::class.java)
            Result.success(states)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Extension function for capitalize since the standard one is deprecated
    private fun String.capitalize(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}
