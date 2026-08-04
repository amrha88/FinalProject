package com.example.automate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.automate.data.repository.FirebaseAuthRepository
import com.example.automate.data.repository.FakeWarningLightRepository
import com.example.automate.ui.screens.*
import com.example.automate.ui.viewmodel.AiAssistantViewModel
import com.example.automate.ui.viewmodel.AuthViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authRepository = remember { FirebaseAuthRepository() }
    val warningLightRepository = remember { FakeWarningLightRepository() }
    
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository) as T
            }
        }
    )

    val aiAssistantViewModel: AiAssistantViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AiAssistantViewModel(warningLightRepository) as T
            }
        }
    )

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
            LoginScreen(
                viewModel = authViewModel,
                onSuccess = { email ->
                    navController.navigate("${Screen.Pin.route}/$email") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onSuccess = { email ->
                    navController.navigate("${Screen.Pin.route}/$email") {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "${Screen.Pin.route}/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            PinScreen(
                email = email,
                onContinue = {
                    navController.navigate(Screen.Biometric.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Biometric.route) {
            BiometricScreen(
                onContinue = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onSkip = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = authViewModel,
                onAddVehicleClick = {
                    navController.navigate(Screen.AddVehicle.route)
                },
                onVehicleClick = { vehicleId ->
                    navController.navigate("vehicle_details/$vehicleId")
                }
            )
        }

        composable(Screen.AddVehicle.route) {
            AddVehicleScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.VehicleDetails.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            VehicleDetailsScreen(
                vehicleId = vehicleId,
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onChatbotClick = { id -> navController.navigate("chatbot/$id") },
                onLicencesClick = { id -> navController.navigate("licences/$id") },
                onNotificationsClick = { id -> navController.navigate("notifications/$id") },
                onHistoryClick = { id -> navController.navigate("history/$id") },
                onAiScannerClick = { navController.navigate(Screen.AiAssistant.route) }
            )
        }

        composable(Screen.AiAssistant.route) {
            AiAssistantScreen(
                viewModel = aiAssistantViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToChat = { result ->
                    // For now, navigating back to chatbot with context if possible, 
                    // or just a TODO as requested.
                    navController.navigate("chatbot/context_${result.id}")
                }
            )
        }

        composable(
            route = Screen.Chatbot.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            ChatbotScreen(vehicleId = vehicleId, onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.Licences.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            LicencesScreen(vehicleId = vehicleId, onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.Notifications.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            NotificationsScreen(vehicleId = vehicleId, onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.History.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            VehicleHistoryScreen(vehicleId = vehicleId, onBackClick = { navController.popBackStack() })
        }
    }
}

