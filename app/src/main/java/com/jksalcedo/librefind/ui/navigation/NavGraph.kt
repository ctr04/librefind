package com.jksalcedo.librefind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jksalcedo.librefind.ui.auth.AuthScreen
import com.jksalcedo.librefind.ui.auth.ProfileSetupScreen
import com.jksalcedo.librefind.ui.dashboard.DashboardScreen
import com.jksalcedo.librefind.ui.details.AlternativeDetailScreen
import com.jksalcedo.librefind.ui.details.DetailsScreen
import com.jksalcedo.librefind.ui.mysubmissions.MySubmissionsScreen
import com.jksalcedo.librefind.ui.settings.IgnoredAppsScreen
import com.jksalcedo.librefind.ui.submit.SubmitScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Route.Dashboard.route
    ) {
        composable(Route.Dashboard.route) {
            DashboardScreen(
                onAppClick = { appName, packageName ->
                    navController.navigate(Route.Details.createRoute(appName, packageName))
                },
                onSubmitClick = {
                    navController.navigate(Route.Auth.route)
                },
                onMySubmissionsClick = {
                    navController.navigate(Route.MySubmissions.route)
                },
                onIgnoredAppsClick = {
                    navController.navigate(Route.IgnoredApps.route)
                }
            )
        }

        composable(
            route = Route.Details.route,
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType },
                navArgument("appName") { type = NavType.StringType })
        ) { backStackEntry ->
            val packageName =
                backStackEntry.arguments?.getString("packageName") ?: return@composable
            val appName = backStackEntry.arguments?.getString("appName") ?: return@composable
            DetailsScreen(
                appName = appName,
                packageName = packageName,
                onBackClick = { navController.navigateUp() },
                onAlternativeClick = { altId ->
                    navController.navigate(Route.AlternativeDetail.createRoute(altId))
                }
            )
        }

        composable(
            route = Route.AlternativeDetail.route,
            arguments = listOf(navArgument("altId") { type = NavType.StringType })
        ) { backStackEntry ->
            val altId = backStackEntry.arguments?.getString("altId") ?: return@composable
            AlternativeDetailScreen(
                altId = altId,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Route.Auth.route) {
            AuthScreen(
                onAuthSuccess = { needsProfile ->
                    if (needsProfile) {
                        navController.navigate(Route.ProfileSetup.route) {
                            popUpTo(Route.Auth.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Route.Submit.route) {
                            popUpTo(Route.Auth.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Route.ProfileSetup.route) {
            ProfileSetupScreen(
                onProfileComplete = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Submit.route) {
            SubmitScreen(
                onBackClick = { navController.navigateUp() },
                onSuccess = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.MySubmissions.route) {
            MySubmissionsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Route.IgnoredApps.route) {
            IgnoredAppsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}


