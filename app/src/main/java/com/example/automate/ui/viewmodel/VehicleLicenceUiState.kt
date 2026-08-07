package com.example.automate.ui.viewmodel

import com.example.automate.domain.model.VehicleLicence
import com.example.automate.domain.model.VehicleLicenceExtraction

data class VehicleLicenceUiState(
    val selectedImageUri: String? = null,
    val isAnalyzing: Boolean = false,
    val extraction: VehicleLicenceExtraction? = null,
    val editableLicence: VehicleLicence? = null,
    val savedLicence: VehicleLicence? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)
