package com.pndnwngi.billumaba.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    
    object AddEdit : Screen("add_edit?visitId={visitId}") {
        fun createRoute(visitId: Long? = null): String {
            return if (visitId != null) "add_edit?visitId=$visitId" else "add_edit"
        }
    }
    
    object Detail : Screen("detail/{visitId}") {
        fun createRoute(visitId: Long): String {
            return "detail/$visitId"
        }
    }
}
