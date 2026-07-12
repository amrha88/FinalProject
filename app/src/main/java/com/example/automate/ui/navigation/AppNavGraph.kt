package com.example.automate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.automate.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToLogin = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        
        composable(Screen.Login.route) {
            LoginScreen(onContinue = { email ->
                navController.navigate("${Screen.Pin.route}/$email")
            })
        }
        
        composable(
            route = "${Screen.Pin.route}/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            PinScreen(email = email, onContinue = {
                navController.navigate(Screen.Biometric.route)
            })
        }
        
        composable(Screen.Biometric.route) {
            BiometricScreen(
                onContinue = { navController.navigate(Screen.HomePlaceholder.route) },
                onSkip = { navController.navigate(Screen.HomePlaceholder.route) }
            )
        }
        
        composable(Screen.HomePlaceholder.route) {
            HomePlaceholderScreen()
        }
    }
}
