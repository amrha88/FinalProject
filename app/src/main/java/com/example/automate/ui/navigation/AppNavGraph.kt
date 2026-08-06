package com.example.automate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.automate.data.repository.FirebaseAuthRepository
import com.example.automate.data.repository.FakeWarningLightRepository
import com.example.automate.data.repository.FirebaseAiChatRepository
import com.example.automate.ui.components.BottomNavBar
import com.example.automate.ui.components.BottomNavItem
import com.example.automate.ui.screens.*
import com.example.automate.ui.viewmodel.AiAssistantViewModel
import com.example.automate.ui.viewmodel.AiChatViewModel
import com.example.automate.ui.viewmodel.AuthViewModel
import com.example.automate.ui.viewmodel.VehicleUiModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authRepository = remember { FirebaseAuthRepository() }
    val warningLightRepository = remember { FakeWarningLightRepository() }
    val aiChatRepository = remember { FirebaseAiChatRepository() }
    
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

    val aiChatViewModel: AiChatViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AiChatViewModel(aiChatRepository) as T
            }
        }
    )

    val authUiState by authViewModel.uiState.collectAsState()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val mainBottomBar: @Composable () -> Unit = {
        BottomNavBar(
            items = listOf(
                BottomNavItem(
                    icon = Icons.Default.DirectionsCar,
                    label = "My Cars",
                    selected = currentRoute == Screen.Home.route,
                    onClick = { navigateToTab(Screen.Home.route) }
                ),
                BottomNavItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "Assistant",
                    selected = currentRoute == Screen.AiAssistant.route,
                    onClick = { navigateToTab(Screen.AiAssistant.route) }
                ),
                BottomNavItem(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { navigateToTab(Screen.Profile.route) }
                ),
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    selected = currentRoute == Screen.Settings.route,
                    onClick = { navigateToTab(Screen.Settings.route) }
                )
            )
        )
    }

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
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
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
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
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
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onNeedHelpClick = {
                    navController.navigate(Screen.AiAssistant.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                bottomBar = mainBottomBar
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                bottomBar = mainBottomBar
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                bottomBar = mainBottomBar
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
                    // Find the vehicle ID if possible or navigate with a generic context
                    // For now, navigating to chatbot without a specific vehicleId or the current one if we knew it.
                    // But chatbot route REQUIRES vehicleId. 
                    // Let's assume we navigate to the generic chatbot or just stay here.
                    // Actually, let's use the first vehicle as a fallback or a TODO.
                    val firstVehicleId = authUiState.vehicles.firstOrNull()?.id ?: "unknown"
                    navController.navigate("chatbot/$firstVehicleId")
                },
                bottomBar = mainBottomBar
            )
        }

        composable(
            route = Screen.Chatbot.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            val vehicle = authUiState.vehicles.find { it.id == vehicleId }?.let {
                VehicleUiModel(
                    id = it.id,
                    manufacturer = it.manufacturer,
                    model = it.model,
                    year = it.year,
                    plate = it.plate,
                    isDark = false
                )
            }
            ChatbotScreen(
                vehicle = vehicle,
                viewModel = aiChatViewModel,
                onBackClick = { navController.popBackStack() }
            )
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
