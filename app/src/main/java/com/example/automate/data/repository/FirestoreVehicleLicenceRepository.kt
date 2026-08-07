package com.example.automate.data.repository

import com.example.automate.domain.model.VehicleLicence
import com.example.automate.domain.repository.VehicleLicenceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreVehicleLicenceRepository : VehicleLicenceRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getLicenceDocument(vehicleId: String) = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vehicles")
            .document(vehicleId)
            .collection("documents")
            .document("vehicleLicence")
    }

    override suspend fun loadLicence(vehicleId: String): Result<VehicleLicence?> {
        return try {
            val doc = getLicenceDocument(vehicleId) ?: throw Exception("User not authenticated")
            val snapshot = doc.get().await()
            if (snapshot.exists()) {
                Result.success(snapshot.toObject(VehicleLicence::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveLicence(vehicleId: String, licence: VehicleLicence): Result<Unit> {
        return try {
            val doc = getLicenceDocument(vehicleId) ?: throw Exception("User not authenticated")
            val data = licence.copy(
                vehicleId = vehicleId,
                updatedAt = System.currentTimeMillis(),
                createdAt = if (licence.createdAt == 0L) System.currentTimeMillis() else licence.createdAt
            )
            doc.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLicence(vehicleId: String): Result<Unit> {
        return try {
            val doc = getLicenceDocument(vehicleId) ?: throw Exception("User not authenticated")
            doc.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
