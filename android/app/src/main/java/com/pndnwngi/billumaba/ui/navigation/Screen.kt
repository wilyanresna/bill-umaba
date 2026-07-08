package com.pndnwngi.billumaba.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AddEdit : Screen("add_edit?visitId={visitId}") {
        fun createRoute(visitId: Long? = null): String {
            return if (visitId != null) "add_edit?visitId=$visitId" else "add_edit"
        }
    }
    data object Detail : Screen("detail/{visitId}") {
        fun createRoute(visitId: Long): String = "detail/$visitId"
    }
    data object OcrReview : Screen("ocr_review")
    data object PatternList : Screen("patterns")
    data object PatternEdit : Screen("patterns/edit?id={id}") {
        fun createRoute(id: Long? = null): String =
            if (id != null) "patterns/edit?id=$id" else "patterns/edit"
    }
}
