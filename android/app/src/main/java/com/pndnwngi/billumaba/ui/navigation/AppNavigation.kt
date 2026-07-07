package com.pndnwngi.billumaba.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddEdit = { visitId ->
                    navController.navigate(Screen.AddEdit.createRoute(visitId))
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
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            AddEditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("visitId") {
                    type = NavType.LongType
                    nullable = false
                }
            )
        ) {
            DetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.AddEdit.createRoute(id))
                }
            )
        }
    }
}
