package com.jksalcedo.librefind.ui.navigation

sealed class Route(val route: String) {
    data object Dashboard : Route("dashboard")
    data object Details : Route("details/{appName}/{packageName}") {
        fun createRoute(appName: String ,packageName: String) = "details/$appName/$packageName"
    }
    data object AlternativeDetail : Route("alternative/{altId}") {
        fun createRoute(altId: String) = "alternative/$altId"
    }
    data object Auth : Route("auth")
    data object ProfileSetup : Route("profile_setup")
    data object Submit : Route("submit")
}


