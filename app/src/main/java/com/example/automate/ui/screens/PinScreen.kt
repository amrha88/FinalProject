package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.ui.theme.AutomateTheme

@Composable
fun PinScreen(email: String, onContinue: () -> Unit, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000C1F))
            .padding(24.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Enter PIN for", color = Color.White, fontSize = 18.sp)
                Text(text = email, color = Color.Cyan, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onContinue) {
                    Text("Continue")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PinScreenPreview() {
    AutomateTheme {
        PinScreen(email = "user@example.com", onContinue = {}, onBackClick = {})
    }
}
