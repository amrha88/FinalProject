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
import com.example.automate.ui.screens.*
import com.example.automate.ui.viewmodel.AuthViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authRepository = remember { FirebaseAuthRepository() }
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository) as T
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToLogin = {
                val startRoute = if (authRepository.isUserLoggedIn()) {
                    // If logged in, we might want to go to PIN or Home, 
                    // but the prompt says "navigate to the existing PIN screen" after login.
                    // Usually splash should check if logged in.
                    Screen.Login.route // Keeping it simple as per instructions "Do not change the rest of the onboarding flow"
                } else {
                    Screen.Login.route
                }
                navController.navigate(startRoute)
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
                onContinue = { navController.navigate(Screen.HomePlaceholder.route) },
                onSkip = { navController.navigate(Screen.HomePlaceholder.route) },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.HomePlaceholder.route) {
            HomePlaceholderScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
