package com.example.automate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.automate.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Every downstream expiry/reminder check parses dates as a strict "yyyy-MM-dd" string.
 * Free-text entry let users type whatever format their car's paperwork uses, which silently
 * failed that parsing and made expired items look active. A picker guarantees the stored
 * format is always correct.
 */
private fun utcDateFormat(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("UTC") }

private fun parseDateToUtcMillis(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return try {
        val sdf = utcDateFormat()
        sdf.isLenient = false
        sdf.parse(value)?.time
    } catch (e: Exception) {
        null
    }
}

private fun formatUtcMillisToDate(millis: Long): String = utcDateFormat().format(Date(millis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateField(
    value: String?,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF007BFF),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF007BFF),
                unfocusedLabelColor = Color.Gray
            ),
            interactionSource = interactionSource
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(interactionSource = interactionSource, indication = null) { showPicker = true }
        )
    }

    if (showPicker) {
        AppDatePickerDialog(
            initialDateString = value,
            onDismiss = { showPicker = false },
            onDateSelected = { date ->
                onValueChange(date)
                showPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateString: String?,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = parseDateToUtcMillis(initialDateString)
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis -> onDateSelected(formatUtcMillisToDate(millis)) }
                    ?: onDismiss()
            }) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    ) {
        DatePicker(state = state)
    }
}
