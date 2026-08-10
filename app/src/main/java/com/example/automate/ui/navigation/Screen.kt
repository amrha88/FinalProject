package com.example.automate.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("sign_up")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object AddVehicle : Screen("add_vehicle")
    object EditVehicle : Screen("edit_vehicle/{vehicleId}")
    object VehicleDetails : Screen("vehicle_details/{vehicleId}")
    object Chatbot : Screen("chatbot/{vehicleId}")
    object Licences : Screen("licences/{vehicleId}")
    object Notifications : Screen("notifications/{vehicleId}")
    object History : Screen("history/{vehicleId}")
    object AiAssistant : Screen("ai_assistant")
    object AiScanner : Screen("ai_scanner/{vehicleId}")
    object VehicleDocuments : Screen("vehicle_documents/{vehicleId}")
}
