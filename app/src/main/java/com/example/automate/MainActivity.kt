package com.example.automate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.automate.ui.navigation.AppNavGraph
import com.example.automate.ui.theme.AutomateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutomateTheme {
                val navController = rememberNavController()
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
