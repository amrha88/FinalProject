package com.example.automate.domain.repository

import com.example.automate.domain.model.VehicleDocument

interface VehicleDocumentRepository {
    suspend fun saveDocument(vehicleId: String, document: VehicleDocument): Result<String>
    suspend fun getDocuments(vehicleId: String): Result<List<VehicleDocument>>
    suspend fun getDocument(vehicleId: String, documentId: String): Result<VehicleDocument>
    suspend fun updateDocument(vehicleId: String, document: VehicleDocument): Result<Unit>
    suspend fun replaceDocument(vehicleId: String, oldDocumentId: String, newDocument: VehicleDocument): Result<String>
    suspend fun deleteDocument(vehicleId: String, documentId: String): Result<Unit>
}
