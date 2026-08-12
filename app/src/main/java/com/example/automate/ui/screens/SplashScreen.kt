package com.example.automate.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.automate.R
import com.example.automate.ui.theme.AutomateTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onNavigateToLogin()
    }

    // Displays the photo as a full-screen image exactly as it was.
    Image(
        painter = painterResource(id = R.drawable.image),
        contentDescription = "Automate Splash",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    AutomateTheme {
        SplashScreen(onNavigateToLogin = {})
    }
}
