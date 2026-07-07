package com.pndnwngi.billumaba.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pndnwngi.billumaba.data.repository.CulinaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: CulinaryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val visitId: Long = savedStateHandle.get<Long>("visitId") ?: -1L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadVisit()
    }

    private fun loadVisit() {
        if (visitId == -1L) return

        repository.getVisitWithMenusById(visitId)
            .onEach { visitWithMenus ->
                visitWithMenus?.let { data ->
                    _uiState.update { state ->
                        state.copy(
                            visit = data.visit,
                            menuItems = data.menuItems,
                            isLoading = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun deleteVisit() {
        val visit = _uiState.value.visit ?: return

        viewModelScope.launch {
            repository.deleteVisit(visit)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
