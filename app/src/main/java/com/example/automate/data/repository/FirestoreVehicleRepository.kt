package com.example.automate.data.repository

import com.example.automate.domain.model.Vehicle
import com.example.automate.domain.model.VehicleSpecs
import com.example.automate.domain.repository.VehicleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
                @Suppress("UNCHECKED_CAST")
                val specsMap = doc.get("specs") as? Map<String, Any?>
                val specs = specsMap?.let {
                    VehicleSpecs(
                        fuelConsumptionL100km = (it["fuelConsumptionL100km"] as? Number)?.toDouble(),
                        fuelType = it["fuelType"] as? String,
                        engineDisplacementL = (it["engineDisplacementL"] as? Number)?.toDouble(),
                        horsepower = (it["horsepower"] as? Number)?.toInt(),
                        transmission = it["transmission"] as? String,
                        fuelTankCapacityL = (it["fuelTankCapacityL"] as? Number)?.toDouble()
                    )
                }
                Vehicle(
                    id = doc.id,
                    manufacturer = doc.getString("manufacturer") ?: return@mapNotNull null,
                    model = doc.getString("model") ?: return@mapNotNull null,
                    year = doc.getString("year") ?: return@mapNotNull null,
                    plate = doc.getString("plate") ?: return@mapNotNull null,
                    photoBase64 = doc.getString("photoBase64"),
                    specs = specs
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
                    "photoBase64" to vehicle.photoBase64,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> {
        return try {
            vehiclesCollection().document(vehicle.id).set(
                mapOf(
                    "manufacturer" to vehicle.manufacturer,
                    "model" to vehicle.model,
                    "year" to vehicle.year,
                    "plate" to vehicle.plate,
                    "photoBase64" to vehicle.photoBase64
                ),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVehicle(vehicleId: String): Result<Unit> {
        return try {
            vehiclesCollection().document(vehicleId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSpecs(vehicleId: String, specs: VehicleSpecs): Result<Unit> {
        return try {
            vehiclesCollection().document(vehicleId).set(
                mapOf(
                    "specs" to mapOf(
                        "fuelConsumptionL100km" to specs.fuelConsumptionL100km,
                        "fuelType" to specs.fuelType,
                        "engineDisplacementL" to specs.engineDisplacementL,
                        "horsepower" to specs.horsepower,
                        "transmission" to specs.transmission,
                        "fuelTankCapacityL" to specs.fuelTankCapacityL
                    )
                ),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}