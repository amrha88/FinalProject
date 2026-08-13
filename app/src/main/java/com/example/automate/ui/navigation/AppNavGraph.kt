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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.automate.R
import com.example.automate.data.repository.*
import com.example.automate.ui.components.BottomNavBar
import com.example.automate.ui.components.BottomNavItem
import com.example.automate.ui.screens.*
import com.example.automate.ui.viewmodel.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authRepository = remember { FirebaseAuthRepository() }
    val warningLightRepository = remember { FirebaseWarningLightRepository() }
    val aiChatRepository = remember { FirebaseAiChatRepository() }
    val vehicleLicenceAnalysisRepository = remember { FirebaseVehicleLicenceAnalysisRepository() }
    val vehicleLicenceRepository = remember { FirestoreVehicleLicenceRepository() }
    val vehicleDocumentAnalysisRepository = remember { FirebaseVehicleDocumentAnalysisRepository() }
    val vehicleDocumentRepository = remember { FirestoreVehicleDocumentRepository() }
    val vehicleHistoryAnalysisRepository = remember { FirebaseVehicleHistoryAnalysisRepository() }
    val vehicleHistoryRepository = remember { FirestoreVehicleHistoryRepository() }
    
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
                return AiAssistantViewModel(warningLightRepository, vehicleHistoryRepository) as T
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

    val vehicleLicenceViewModel: VehicleLicenceViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VehicleLicenceViewModel(
                    vehicleLicenceAnalysisRepository,
                    vehicleLicenceRepository
                ) as T
            }
        }
    )

    val vehicleDocumentsViewModel: VehicleDocumentsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VehicleDocumentsViewModel(
                    vehicleDocumentAnalysisRepository,
                    vehicleDocumentRepository,
                    vehicleHistoryRepository
                ) as T
            }
        }
    )



    val vehicleHistoryViewModel: VehicleHistoryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VehicleHistoryViewModel(
                    vehicleHistoryRepository,
                    vehicleHistoryAnalysisRepository
                ) as T
            }
        }
    )

    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificationsViewModel(
                    reminderRepository = FirestoreReminderRepository()
                ) as T
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
                    label = stringResource(R.string.nav_my_cars),
                    selected = currentRoute == Screen.Home.route,
                    onClick = { navigateToTab(Screen.Home.route) }
                ),
                BottomNavItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = stringResource(R.string.chat_ai_assistant_title),
                    selected = currentRoute?.startsWith("ai_assistant") == true,
                    onClick = { 
                        val firstVehicleId = authUiState.vehicles.firstOrNull()?.id ?: "unknown"
                        navigateToTab("ai_assistant/$firstVehicleId") 
                    }
                ),
                BottomNavItem(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.settings_profile_title),
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { navigateToTab(Screen.Profile.route) }
                ),
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.settings_title),
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
                val startRoute = if (authRepository.isUserLoggedIn()) Screen.Home.route else Screen.Login.route
                navController.navigate(startRoute) {
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
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onBackToSignIn = { navController.popBackStack() }
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
                onNeedHelpClick = {
                    val firstVehicleId = authUiState.vehicles.firstOrNull()?.id ?: "unknown"
                    navController.navigate("ai_assistant/$firstVehicleId")
                },
                bottomBar = mainBottomBar
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = authViewModel,
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onAccountDeleted = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                bottomBar = mainBottomBar
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                bottomBar = mainBottomBar
            )
        }

        composable(
            route = Screen.VehicleSetup.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            VehicleSetupQuestionsScreen(
                onComplete = { inspectionDate, inspectionPrecision, licenceDate, licencePrecision ->
                    authViewModel.saveInitialSetup(vehicleId, inspectionDate, inspectionPrecision, licenceDate, licencePrecision)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddVehicle.route) {
            AddVehicleScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { vehicleId ->
                    navController.navigate("vehicle_setup/$vehicleId") {
                        popUpTo(Screen.AddVehicle.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.EditVehicle.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            AddVehicleScreen(
                viewModel = authViewModel,
                editingVehicleId = vehicleId,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
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
                onDocumentsClick = { id -> navController.navigate("vehicle_documents/$id") },
                onAiScannerClick = { navController.navigate("ai_assistant/$vehicleId") },
                onEditClick = { id -> navController.navigate("edit_vehicle/$id") },
                bottomBar = mainBottomBar
            )
        }

        composable(
            route = Screen.VehicleDocuments.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            val vehicle = authUiState.vehicles.find { it.id == vehicleId }?.let {
                com.example.automate.domain.model.Vehicle(
                    id = it.id,
                    manufacturer = it.manufacturer,
                    model = it.model,
                    year = it.year,
                    plate = it.plate,
                    isDark = it.isDark
                )
            }
            VehicleDocumentsScreen(
                vehicleId = vehicleId,
                vehicle = vehicle,
                viewModel = vehicleDocumentsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AiAssistant.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            val openChat = {
                navController.navigate("chatbot/$vehicleId")
            }
            AiAssistantScreen(
                vehicleId = vehicleId,
                viewModel = aiAssistantViewModel,
                onNavigateToChat = { openChat() },
                onOpenChatClick = openChat,
                onBackClick = { navController.popBackStack() },
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
            val vehicle = authUiState.vehicles.find { it.id == vehicleId }?.let {
                com.example.automate.domain.model.Vehicle(
                    id = it.id,
                    manufacturer = it.manufacturer,
                    model = it.model,
                    year = it.year,
                    plate = it.plate,
                    isDark = it.isDark
                )
            }
            VehicleLicenceScreen(
                vehicleId = vehicleId,
                vehicle = vehicle,
                viewModel = vehicleLicenceViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Notifications.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            NotificationsScreen(
                vehicleId = vehicleId,
                viewModel = notificationsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.History.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            val vehicle = authUiState.vehicles.find { it.id == vehicleId }?.let {
                com.example.automate.domain.model.Vehicle(
                    id = it.id,
                    manufacturer = it.manufacturer,
                    model = it.model,
                    year = it.year,
                    plate = it.plate,
                    isDark = it.isDark
                )
            }
            VehicleHistoryScreen(
                vehicleId = vehicleId,
                vehicle = vehicle,
                viewModel = vehicleHistoryViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
