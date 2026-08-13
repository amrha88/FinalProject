package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.R
import com.example.automate.domain.model.DatePrecision
import com.example.automate.ui.components.PrimaryButton
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VehicleSetupQuestionsScreen(
    onComplete: (inspectionDate: String?, inspectionPrecision: DatePrecision, licenceDate: String?, licencePrecision: DatePrecision) -> Unit
) {
    var inspectionStep by remember { mutableStateOf(true) }
    
    var inspectionDate by remember { mutableStateOf<String?>(null) }
    var inspectionPrecision by remember { mutableStateOf(DatePrecision.UNKNOWN) }
    
    var licenceDate by remember { mutableStateOf<String?>(null) }
    var licencePrecision by remember { mutableStateOf(DatePrecision.UNKNOWN) }

    Scaffold(
        containerColor = Color(0xFF000C1F)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (inspectionStep) {
                SetupQuestion(
                    title = stringResource(R.string.setup_inspection_title),
                    question = stringResource(R.string.setup_inspection_question),
                    onDateSelected = { date, precision ->
                        inspectionDate = date
                        inspectionPrecision = precision
                        inspectionStep = false
                    }
                )
            } else {
                SetupQuestion(
                    title = stringResource(R.string.doc_type_vehicle_licence),
                    question = stringResource(R.string.setup_licence_question),
                    onDateSelected = { date, precision ->
                        licenceDate = date
                        licencePrecision = precision
                        onComplete(inspectionDate, inspectionPrecision, licenceDate, licencePrecision)
                    }
                )
            }
        }
    }
}

@Composable
private fun SetupQuestion(
    title: String,
    question: String,
    onDateSelected: (String?, DatePrecision) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    Text(text = title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = question, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(32.dp))

    PrimaryButton(text = stringResource(R.string.action_exact_date), onClick = { showDatePicker = true })
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = {
            // Mocking month selection for now
            val calendar = Calendar.getInstance()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendar.time)
            onDateSelected(date, DatePrecision.MONTH_ONLY)
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(stringResource(R.string.action_month_only), color = Color.White)
    }
    Spacer(modifier = Modifier.height(12.dp))

    TextButton(onClick = { onDateSelected(null, DatePrecision.UNKNOWN) }) {
        Text(stringResource(R.string.action_dont_know), color = Color.Gray)
    }

    if (showDatePicker) {
        // Simplified DatePicker for the task
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendar.time)
        onDateSelected(date, DatePrecision.EXACT)
        showDatePicker = false
    }
}
