package com.pndnwngi.billumaba.ui.patterns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pndnwngi.billumaba.data.database.dao.ReceiptPatternDao
import com.pndnwngi.billumaba.data.database.entities.ReceiptPatternEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatternListViewModel @Inject constructor(
    private val patternDao: ReceiptPatternDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternListUiState())
    val uiState: StateFlow<PatternListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            patternDao.observeAll().collect { patterns ->
                _uiState.update { it.copy(patterns = patterns, isLoading = false) }
            }
        }
    }

    fun confirmDelete(id: Long) {
        _uiState.update { it.copy(pendingDeleteId = id) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDeleteId = null) }
    }

    fun deletePattern(pattern: ReceiptPatternEntity) {
        viewModelScope.launch {
            patternDao.delete(pattern)
            _uiState.update { it.copy(pendingDeleteId = null) }
        }
    }
}
