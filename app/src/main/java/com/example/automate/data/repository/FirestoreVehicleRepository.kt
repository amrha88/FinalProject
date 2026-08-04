package com.example.automate.data.repository

import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.repository.VehicleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreVehicleRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : VehicleRepository {

    private fun vehiclesCollection() = firestore.collection("users")
        .document(firebaseAuth.currentUser?.uid ?: throw IllegalStateException("No signed-in user."))
        .collection("vehicles")

    override suspend fun getVehicles(): Result<List<Vehicle>> {
        return try {
            val snapshot = vehiclesCollection().get().await()
            val vehicles = snapshot.documents.mapNotNull { doc ->
                Vehicle(
                    id = doc.id,
                    manufacturer = doc.getString("manufacturer") ?: return@mapNotNull null,
                    model = doc.getString("model") ?: return@mapNotNull null,
                    year = doc.getString("year") ?: return@mapNotNull null,
                    plate = doc.getString("plate") ?: return@mapNotNull null
                )
            }
            Result.success(vehicles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addVehicle(vehicle: Vehicle): Result<Unit> {
        return try {
            vehiclesCollection().document(vehicle.id).set(
                mapOf(
                    "manufacturer" to vehicle.manufacturer,
                    "model" to vehicle.model,
                    "year" to vehicle.year,
                    "plate" to vehicle.plate,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}