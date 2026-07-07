package com.pndnwngi.billumaba.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pndnwngi.billumaba.data.database.entities.VisitWithMenus
import com.pndnwngi.billumaba.data.repository.CulinaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: CulinaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _sortType = MutableStateFlow(SortType.DATE_NEWEST)

    init {
        observeData()
    }

    private fun observeData() {
        combine(_searchQuery, _sortType) { query, sort ->
            Pair(query, sort)
        }.flatMapLatest { (query, sort) ->
            if (query.isBlank()) {
                repository.getAllVisitsWithMenus()
            } else {
                repository.searchVisits(query)
            }.combine(MutableStateFlow(sort)) { visits, sortType ->
                sortVisits(visits, sortType)
            }
        }.onEach { sortedVisits ->
            val now = Calendar.getInstance()
            val currentMonth = now.get(Calendar.MONTH)
            val currentYear = now.get(Calendar.YEAR)

            val monthlyExpense = sortedVisits
                .filter { visitWithMenus ->
                    val visitDate = Calendar.getInstance().apply {
                        timeInMillis = visitWithMenus.visit.visitDate
                    }
                    visitDate.get(Calendar.MONTH) == currentMonth &&
                    visitDate.get(Calendar.YEAR) == currentYear
                }
                .sumOf { it.visit.grandTotal }

            _uiState.update { state ->
                state.copy(
                    monthlyExpense = monthlyExpense,
                    totalVisits = sortedVisits.size,
                    visits = sortedVisits,
                    searchQuery = _searchQuery.value,
                    sortType = _sortType.value,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun sortVisits(visits: List<VisitWithMenus>, sortType: SortType): List<VisitWithMenus> {
        return when (sortType) {
            SortType.DATE_NEWEST -> visits.sortedByDescending { it.visit.visitDate }
            SortType.DATE_OLDEST -> visits.sortedBy { it.visit.visitDate }
            SortType.TOTAL_HIGHEST -> visits.sortedByDescending { it.visit.grandTotal }
            SortType.TOTAL_LOWEST -> visits.sortedBy { it.visit.grandTotal }
            SortType.RATING_HIGHEST -> visits.sortedByDescending { it.visit.restaurantRating }
            SortType.RATING_LOWEST -> visits.sortedBy { it.visit.restaurantRating }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSortTypeChanged(sortType: SortType) {
        _sortType.value = sortType
    }

    fun deleteVisit(visitWithMenus: VisitWithMenus) {
        viewModelScope.launch {
            repository.deleteVisit(visitWithMenus.visit)
        }
    }
}
