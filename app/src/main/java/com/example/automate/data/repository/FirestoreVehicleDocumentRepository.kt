package com.example.automate.data.repository

import android.util.Log
import com.example.automate.domain.model.*
import com.example.automate.domain.repository.VehicleDocumentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreVehicleDocumentRepository : VehicleDocumentRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreDocRepo"

    private fun getDocumentsCollection(vehicleId: String) = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vehicles")
            .document(vehicleId)
            .collection("documents")
    }

    private fun getHistoryCollection(vehicleId: String) = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vehicles")
            .document(vehicleId)
            .collection("history")
    }

    private fun getRemindersCollection(vehicleId: String) = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vehicles")
            .document(vehicleId)
            .collection("reminders")
    }

    override suspend fun saveDocument(vehicleId: String, document: VehicleDocument): Result<String> {
        Log.d(TAG, "DOCUMENT_SAVE_START: uid=${auth.currentUser?.uid}, vehicleId=$vehicleId, type=${document.documentType}")
        return try {
            val collection = getDocumentsCollection(vehicleId) ?: throw Exception("User not authenticated")
            val newDocRef = collection.document()
            val data = document.copy(
                id = newDocRef.id,
                vehicleId = vehicleId,
                status = VehicleDocumentStatus.ACTIVE,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            newDocRef.set(data).await()
            Log.d(TAG, "DOCUMENT_SAVE_SUCCESS: documentId=${newDocRef.id}")
            Result.success(newDocRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "DOCUMENT_SAVE_FAILURE: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getDocuments(vehicleId: String): Result<List<VehicleDocument>> {
        return try {
            val collection = getDocumentsCollection(vehicleId) ?: throw Exception("User not authenticated")
            val snapshot = collection.orderBy("documentDate", Query.Direction.DESCENDING).get().await()
            val docs = snapshot.toObjects(VehicleDocument::class.java)
            Result.success(docs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDocument(vehicleId: String, documentId: String): Result<VehicleDocument> {
        return try {
            val collection = getDocumentsCollection(vehicleId) ?: throw Exception("User not authenticated")
            val snapshot = collection.document(documentId).get().await()
            val doc = snapshot.toObject(VehicleDocument::class.java) ?: throw Exception("Document not found")
            Result.success(doc)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDocument(vehicleId: String, document: VehicleDocument): Result<Unit> {
        return try {
            val collection = getDocumentsCollection(vehicleId) ?: throw Exception("User not authenticated")
            val data = document.copy(updatedAt = System.currentTimeMillis())
            collection.document(document.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun replaceDocument(
        vehicleId: String,
        oldDocumentId: String,
        newDocument: VehicleDocument,
        historyEvent: VehicleHistoryEvent?,
        reminders: List<VehicleReminder>
    ): Result<String> {
        Log.d(TAG, "DOCUMENT_REPLACE_START: oldDoc=$oldDocumentId, vehicleId=$vehicleId")
        return try {
            val docCollection = getDocumentsCollection(vehicleId) ?: throw Exception("User not authenticated")
            val historyCollection = getHistoryCollection(vehicleId) ?: throw Exception("User not authenticated")
            val remindersCollection = getRemindersCollection(vehicleId) ?: throw Exception("User not authenticated")
            val maintenanceCollection = firestore.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("vehicles")
                .document(vehicleId)
                .collection("maintenanceState")
            
            val batch = firestore.batch()
            val oldDocRef = docCollection.document(oldDocumentId)
            val newDocRef = docCollection.document()
            
            val updatedNewDoc = newDocument.copy(
                id = newDocRef.id,
                vehicleId = vehicleId,
                status = VehicleDocumentStatus.ACTIVE,
                replacesDocumentId = oldDocumentId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            batch.set(newDocRef, updatedNewDoc)
            batch.update(oldDocRef, "status", VehicleDocumentStatus.REPLACED, "replacedByDocumentId", newDocRef.id, "updatedAt", System.currentTimeMillis())

            // Deactivate old reminders for the types we're replacing
            if (reminders.isNotEmpty()) {
                val types = reminders.map { it.type.name }.distinct()
                val oldReminders = remindersCollection
                    .whereEqualTo("status", ReminderStatus.ACTIVE.name)
                    .whereIn("type", types)
                    .get().await()
                for (doc in oldReminders.documents) {
                    batch.update(doc.reference, "status", ReminderStatus.REPLACED.name, "updatedAt", System.currentTimeMillis())
                }
            }

            historyEvent?.let { event ->
                Log.d(TAG, "HISTORY_SYNC_START (REPLACE)")
                val eventRef = historyCollection.document()
                val finalEvent = event.copy(id = eventRef.id, vehicleId = vehicleId, sourceDocumentId = newDocRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
                batch.set(eventRef, finalEvent)

                // Update maintenanceState
                event.maintenanceItems.forEach { item ->
                    val stateRef = maintenanceCollection.document(item.type.name)
                    val newState = VehicleMaintenanceItemState(
                        type = item.type,
                        lastAction = try { MaintenanceAction.valueOf(item.action?.uppercase() ?: "CHECKED") } catch(e: Exception) { MaintenanceAction.CHECKED },
                        lastServiceDate = event.eventDate,
                        lastServiceMileage = event.mileage,
                        sourceHistoryEventId = eventRef.id,
                        updatedAt = System.currentTimeMillis()
                    )
                    batch.set(stateRef, newState)
                }
            }

            reminders.forEach { reminder ->
                Log.d(TAG, "REMINDER_SYNC_START (REPLACE)")
                val reminderRef = remindersCollection.document()
                batch.set(reminderRef, reminder.copy(id = reminderRef.id, vehicleId = vehicleId, sourceDocumentId = newDocRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
            }

            batch.commit().await()
            Log.d(TAG, "DOCUMENT_REPLACE_SUCCESS")
            Result.success(newDocRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "DOCUMENT_REPLACE_FAILURE: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun saveConfirmedDocumentAtomic(
        vehicleId: String,
        document: VehicleDocument,
        historyEvent: VehicleHistoryEvent?,
        reminders: List<VehicleReminder>
    ): Result<String> {
        Log.d(TAG, "DOCUMENT_SAVE_START (ATOMIC): vehicleId=$vehicleId, type=${document.documentType}")
        return try {
            val docCollection = getDocumentsCollection(vehicleId) ?: throw Exception("User not authenticated")
            val historyCollection = getHistoryCollection(vehicleId) ?: throw Exception("User not authenticated")
            val remindersCollection = getRemindersCollection(vehicleId) ?: throw Exception("User not authenticated")
            val maintenanceCollection = firestore.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("vehicles")
                .document(vehicleId)
                .collection("maintenanceState")

            val batch = firestore.batch()
            val docRef = if (document.id.isEmpty()) docCollection.document() else docCollection.document(document.id)
            val finalDoc = document.copy(
                id = docRef.id,
                vehicleId = vehicleId,
                status = VehicleDocumentStatus.ACTIVE,
                createdAt = if (document.createdAt == 0L) System.currentTimeMillis() else document.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            batch.set(docRef, finalDoc)

            if (reminders.isNotEmpty()) {
                val types = reminders.map { it.type.name }.distinct()
                val oldReminders = remindersCollection
                    .whereEqualTo("status", ReminderStatus.ACTIVE.name)
                    .whereIn("type", types)
                    .get().await()
                for (doc in oldReminders.documents) {
                    batch.update(doc.reference, "status", ReminderStatus.REPLACED.name, "updatedAt", System.currentTimeMillis())
                }
            }

            historyEvent?.let { event ->
                Log.d(TAG, "HISTORY_SYNC_START")
                val eventRef = historyCollection.document()
                val finalEvent = event.copy(id = eventRef.id, vehicleId = vehicleId, sourceDocumentId = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
                batch.set(eventRef, finalEvent)

                // Update maintenanceState
                event.maintenanceItems.forEach { item ->
                    val stateRef = maintenanceCollection.document(item.type.name)
                    val newState = VehicleMaintenanceItemState(
                        type = item.type,
                        lastAction = try { MaintenanceAction.valueOf(item.action?.uppercase() ?: "CHECKED") } catch(e: Exception) { MaintenanceAction.CHECKED },
                        lastServiceDate = event.eventDate,
                        lastServiceMileage = event.mileage,
                        sourceHistoryEventId = eventRef.id,
                        updatedAt = System.currentTimeMillis()
                    )
                    batch.set(stateRef, newState)
                }
            }

            reminders.forEach { reminder ->
                Log.d(TAG, "REMINDER_SYNC_START")
                val reminderRef = remindersCollection.document()
                batch.set(reminderRef, reminder.copy(id = reminderRef.id, vehicleId = vehicleId, sourceDocumentId = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
            }

            batch.commit().await()
            Log.d(TAG, "DOCUMENT_SAVE_SUCCESS (ATOMIC)")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "DOCUMENT_SAVE_FAILURE (ATOMIC): ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteDocument(vehicleId: String, documentId: String): Result<Unit> {
        return try {
            val collection = getDocumentsCollection(vehicleId) ?: throw Exception("User not authenticated")
            collection.document(documentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
