package com.example.automate.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("sign_up")
    object Biometric : Screen("biometric")
    object HomePlaceholder : Screen("home_placeholder")
}
