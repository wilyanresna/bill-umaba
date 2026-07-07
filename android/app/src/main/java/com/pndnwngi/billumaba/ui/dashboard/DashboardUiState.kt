package com.pndnwngi.billumaba.ui.dashboard

import com.pndnwngi.billumaba.data.database.entities.VisitWithMenus

enum class SortOrder {
    DATE_DESC, // Terbaru
    DATE_ASC,  // Terlama
    PRICE_DESC, // Termahal
    PRICE_ASC,  // Termurah
    RATING_DESC, // Rating Tertinggi
    RATING_ASC   // Rating Terendah
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val visits: List<VisitWithMenus> = emptyList(),
    val totalExpenseThisMonth: Double = 0.0,
    val totalVisitsCount: Int = 0,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val errorMessage: String? = null
)
