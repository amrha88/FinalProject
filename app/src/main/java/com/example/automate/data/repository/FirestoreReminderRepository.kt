package com.example.automate.data.repository

import com.example.automate.domain.model.ReminderStatus
import com.example.automate.domain.model.ReminderType
import com.example.automate.domain.model.VehicleReminder
import com.example.automate.domain.repository.ReminderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreReminderRepository : ReminderRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getVehicleRemindersCollection(vehicleId: String) = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vehicles")
            .document(vehicleId)
            .collection("reminders")
    }

    private fun getUserRemindersCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("reminders")
    }

    override suspend fun getVehicleReminders(vehicleId: String): Result<List<VehicleReminder>> {
        return try {
            val collection = getVehicleRemindersCollection(vehicleId) ?: throw Exception("User not authenticated")
            val snapshot = collection.whereEqualTo("status", ReminderStatus.ACTIVE.name).get().await()
            val reminders = snapshot.toObjects(VehicleReminder::class.java)
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserReminders(): Result<List<VehicleReminder>> {
        return try {
            val collection = getUserRemindersCollection() ?: throw Exception("User not authenticated")
            val snapshot = collection.whereEqualTo("status", ReminderStatus.ACTIVE.name).get().await()
            val reminders = snapshot.toObjects(VehicleReminder::class.java)
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveReminder(reminder: VehicleReminder): Result<String> {
        return try {
            val collection = if (reminder.vehicleId.isNotEmpty()) {
                getVehicleRemindersCollection(reminder.vehicleId)
            } else {
                getUserRemindersCollection()
            } ?: throw Exception("User not authenticated")

            val docRef = if (reminder.id.isEmpty()) collection.document() else collection.document(reminder.id)
            val finalReminder = reminder.copy(
                id = docRef.id,
                createdAt = if (reminder.createdAt == 0L) System.currentTimeMillis() else reminder.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            docRef.set(finalReminder).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactiveOldReminders(vehicleId: String, type: ReminderType): Result<Unit> {
        return try {
            val collection = getVehicleRemindersCollection(vehicleId) ?: throw Exception("User not authenticated")
            val snapshot = collection
                .whereEqualTo("type", type.name)
                .whereEqualTo("status", ReminderStatus.ACTIVE.name)
                .get().await()

            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "status", ReminderStatus.REPLACED.name, "updatedAt", System.currentTimeMillis())
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReminder(vehicleId: String, reminderId: String): Result<Unit> {
        return try {
            val collection = if (vehicleId.isNotEmpty()) {
                getVehicleRemindersCollection(vehicleId)
            } else {
                getUserRemindersCollection()
            } ?: throw Exception("User not authenticated")
            
            collection.document(reminderId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
