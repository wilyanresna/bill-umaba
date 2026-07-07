package com.pndnwngi.billumaba.ui.dashboard

import com.pndnwngi.billumaba.data.database.entities.VisitWithMenus

enum class SortType {
    DATE_NEWEST,
    DATE_OLDEST,
    TOTAL_HIGHEST,
    TOTAL_LOWEST,
    RATING_HIGHEST,
    RATING_LOWEST
}

data class DashboardUiState(
    val monthlyExpense: Double = 0.0,
    val totalVisits: Int = 0,
    val searchQuery: String = "",
    val sortType: SortType = SortType.DATE_NEWEST,
    val visits: List<VisitWithMenus> = emptyList(),
    val isLoading: Boolean = true
)
