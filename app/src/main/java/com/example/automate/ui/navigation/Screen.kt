package com.example.automate.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Pin : Screen("pin")
    object Biometric : Screen("biometric")
    object HomePlaceholder : Screen("home_placeholder")
}
