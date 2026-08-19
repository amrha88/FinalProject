package com.example.automate

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.example.automate.ui.navigation.AppNavGraph
import com.example.automate.ui.theme.AutomateTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Hebrew only translates text; layout, icons, and nav bar stay left-to-right.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                AutomateTheme {
                    val navController = rememberNavController()
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
