package com.pndnwngi.billumaba.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pndnwngi.billumaba.data.ocr.OcrResult
import com.pndnwngi.billumaba.data.parser.ParsedReceipt
import com.pndnwngi.billumaba.ui.addedit.AddEditScreen
import com.pndnwngi.billumaba.ui.addedit.AddEditViewModel
import com.pndnwngi.billumaba.ui.dashboard.DashboardScreen
import com.pndnwngi.billumaba.ui.detail.DetailScreen
import com.pndnwngi.billumaba.ui.ocr.OcrReviewScreen
import com.pndnwngi.billumaba.ui.patterns.PatternEditScreen
import com.pndnwngi.billumaba.ui.patterns.PatternListScreen

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    // Shared OCR result between AddEditScreen and OcrReviewScreen.
    // Stored at NavHost level so both composables can access it.
    var pendingOcrResult by rememberSaveable { mutableStateOf<OcrResult?>(null) }

    // Shared parsed receipt from OcrReviewScreen back to AddEditScreen.
    var pendingParsedReceipt by rememberSaveable { mutableStateOf<ParsedReceipt?>(null) }

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
                },
                onNavigateToPatterns = {
                    navController.navigate(Screen.PatternList.route)
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
        ) { entry ->
            // Retrieve the AddEditViewModel scoped to this composable.
            // We use hiltViewModel() via AddEditScreen, but need access here
            // to apply the parsed receipt from OcrReviewScreen.
            val addEditViewModel: AddEditViewModel = androidx.hilt.navigation.compose.hiltViewModel(entry)

            // Consume pending parsed receipt if any
            LaunchedEffect(pendingParsedReceipt) {
                val parsed = pendingParsedReceipt
                if (parsed != null) {
                    addEditViewModel.applyParsedReceipt(parsed)
                    pendingParsedReceipt = null
                }
            }

            AddEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOcrReview = { ocrResult ->
                    pendingOcrResult = ocrResult
                    navController.navigate(Screen.OcrReview.route)
                },
                viewModel = addEditViewModel
            )
        }

        composable(route = Screen.OcrReview.route) {
            OcrReviewScreen(
                ocrResult = pendingOcrResult,
                onNavigateBack = { navController.popBackStack() },
                onApplyParsedReceipt = { parsed ->
                    pendingParsedReceipt = parsed
                    navController.popBackStack()
                }
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

        composable(route = Screen.PatternList.route) {
            PatternListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.PatternEdit.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.PatternEdit.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            PatternEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
