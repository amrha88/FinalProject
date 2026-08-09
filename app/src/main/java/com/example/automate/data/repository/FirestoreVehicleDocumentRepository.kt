package com.example.automate.data.repository

import com.example.automate.domain.model.VehicleDocument
import com.example.automate.domain.model.VehicleDocumentStatus
import com.example.automate.domain.repository.VehicleDocumentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreVehicleDocumentRepository : VehicleDocumentRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getDocumentsCollection(vehicleId: String) = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vehicles")
            .document(vehicleId)
            .collection("documents")
    }

    override suspend fun saveDocument(vehicleId: String, document: VehicleDocument): Result<String> {
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
            Result.success(newDocRef.id)
        } catch (e: Exception) {
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

    override suspend fun replaceDocument(vehicleId: String, oldDocumentId: String, newDocument: VehicleDocument): Result<String> {
        return try {
            val collection = getDocumentsCollection(vehicleId) ?: throw Exception("User not authenticated")
            val batch = firestore.batch()
            
            val oldDocRef = collection.document(oldDocumentId)
            val newDocRef = collection.document()
            
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
            
            batch.commit().await()
            Result.success(newDocRef.id)
        } catch (e: Exception) {
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
