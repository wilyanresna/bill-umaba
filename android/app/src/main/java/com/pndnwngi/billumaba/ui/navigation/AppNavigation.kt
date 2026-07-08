package com.pndnwngi.billumaba.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pndnwngi.billumaba.ui.addedit.AddEditScreen
import com.pndnwngi.billumaba.ui.dashboard.DashboardScreen
import com.pndnwngi.billumaba.ui.detail.DetailScreen

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddEdit.createRoute())
                },
                onNavigateToDetail = { visitId ->
                    navController.navigate(Screen.Detail.createRoute(visitId))
                }
            )
        }

        composable(
            route = Screen.AddEdit.route,
            arguments = listOf(
                navArgument("visitId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("visitId") {
                    type = NavType.LongType
                }
            )
        ) {
            DetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { visitId ->
                    navController.navigate(Screen.AddEdit.createRoute(visitId))
                }
            )
        }
    }
}
