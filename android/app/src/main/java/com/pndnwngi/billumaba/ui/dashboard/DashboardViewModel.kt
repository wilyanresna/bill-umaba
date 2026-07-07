package com.pndnwngi.billumaba.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pndnwngi.billumaba.data.database.entities.VisitEntity
import com.pndnwngi.billumaba.data.database.entities.VisitWithMenus
import com.pndnwngi.billumaba.data.repository.CulinaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: CulinaryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder = _sortOrder

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredVisitsFlow = _searchQuery.flatMapLatest { query ->
        repository.searchVisits(query)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllVisitsWithMenus(),
        filteredVisitsFlow,
        _sortOrder,
        _searchQuery
    ) { allVisits, filteredVisits, sort, query ->
        // Calculate monthly expense for current month
        val currentCal = Calendar.getInstance()
        val currentMonth = currentCal.get(Calendar.MONTH)
        val currentYear = currentCal.get(Calendar.YEAR)

        val visitCal = Calendar.getInstance()
        val totalExpenseThisMonth = allVisits.filter {
            visitCal.timeInMillis = it.visit.visitDate
            visitCal.get(Calendar.MONTH) == currentMonth && visitCal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.visit.grandTotal }

        // Sort the filtered list of visits
        val sortedVisits = when (sort) {
            SortOrder.DATE_DESC -> filteredVisits.sortedByDescending { it.visit.visitDate }
            SortOrder.DATE_ASC -> filteredVisits.sortedBy { it.visit.visitDate }
            SortOrder.PRICE_DESC -> filteredVisits.sortedByDescending { it.visit.grandTotal }
            SortOrder.PRICE_ASC -> filteredVisits.sortedBy { it.visit.grandTotal }
            SortOrder.RATING_DESC -> filteredVisits.sortedByDescending { it.visit.restaurantRating }
            SortOrder.RATING_ASC -> filteredVisits.sortedBy { it.visit.restaurantRating }
        }

        DashboardUiState(
            isLoading = false,
            visits = sortedVisits,
            totalExpenseThisMonth = totalExpenseThisMonth,
            totalVisitsCount = allVisits.size,
            searchQuery = query,
            sortOrder = sort
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChange(order: SortOrder) {
        _sortOrder.value = order
    }

    fun deleteVisit(visit: VisitEntity) {
        viewModelScope.launch {
            repository.deleteVisit(visit)
        }
    }
}
