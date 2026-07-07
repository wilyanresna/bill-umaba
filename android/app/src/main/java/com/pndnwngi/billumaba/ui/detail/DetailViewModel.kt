package com.pndnwngi.billumaba.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pndnwngi.billumaba.data.repository.CulinaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: CulinaryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        val visitId = savedStateHandle.get<Long>("visitId")
            ?: savedStateHandle.get<String>("visitId")?.toLongOrNull()
        if (visitId != null) {
            loadVisit(visitId)
        } else {
            _uiState.value = DetailUiState(errorMessage = "ID Kunjungan tidak valid")
        }
    }

    private fun loadVisit(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getVisitWithMenusById(id)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal memuat rincian kunjungan"
                    )
                }
                .collect { visitWithMenus ->
                    if (visitWithMenus != null) {
                        _uiState.value = DetailUiState(
                            isLoading = false,
                            visitWithMenus = visitWithMenus
                        )
                    } else {
                        _uiState.value = DetailUiState(
                            isLoading = false,
                            errorMessage = "Data kunjungan tidak ditemukan"
                        )
                    }
                }
        }
    }

    fun deleteVisit() {
        val visit = _uiState.value.visitWithMenus?.visit ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            try {
                repository.deleteVisit(visit)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    isDeleteSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = e.message ?: "Gagal menghapus kunjungan"
                )
            }
        }
    }
}
