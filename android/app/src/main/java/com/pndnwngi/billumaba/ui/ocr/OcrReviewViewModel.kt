package com.pndnwngi.billumaba.ui.ocr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pndnwngi.billumaba.data.ocr.OcrLine
import com.pndnwngi.billumaba.data.ocr.OcrResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for OcrReviewScreen.
 *
 * Tahap 2: applyToForm() is a stub (navigates back without parsing).
 * Tahap 3: will inject ReceiptParserFactory to map OCR lines → ParsedReceipt.
 */
@HiltViewModel
class OcrReviewViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OcrReviewUiState())
    val uiState: StateFlow<OcrReviewUiState> = _uiState.asStateFlow()

    fun loadOcrResult(ocrResult: OcrResult?) {
        if (ocrResult == null) {
            _uiState.update { it.copy(errorMessage = "Tidak ada hasil OCR") }
            return
        }
        val lines = ocrResult.lines.map { it.text }
        _uiState.update {
            it.copy(
                ocrResult = ocrResult,
                editedLines = lines
            )
        }
    }

    fun updateLineText(index: Int, newText: String) {
        _uiState.update { state ->
            val updated = state.editedLines.toMutableList()
            if (index in updated.indices) {
                updated[index] = newText
            }
            state.copy(editedLines = updated)
        }
    }

    fun applyToForm(onApplied: () -> Unit) {
        // Tahap 2 stub: navigate back without parsing.
        // Tahap 3 will: build OcrResult from editedLines, run parser, populate AddEditViewModel.
        onApplied()
    }
}
