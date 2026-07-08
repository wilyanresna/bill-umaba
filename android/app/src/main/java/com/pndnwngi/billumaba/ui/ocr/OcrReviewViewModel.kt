package com.pndnwngi.billumaba.ui.ocr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pndnwngi.billumaba.data.ocr.OcrLine
import com.pndnwngi.billumaba.data.ocr.OcrResult
import com.pndnwngi.billumaba.data.parser.ParsedReceipt
import com.pndnwngi.billumaba.data.parser.ParserType
import com.pndnwngi.billumaba.data.parser.ReceiptParserFactory
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
 * Tahap 3: Injects ReceiptParserFactory to map OCR lines → ParsedReceipt.
 * The parsed result is exposed via [uiState] and passed back to AddEditScreen
 * via a callback when the user taps "Pakai Hasil Scan".
 */
@HiltViewModel
class OcrReviewViewModel @Inject constructor(
    private val parserFactory: ReceiptParserFactory
) : ViewModel() {

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
        // Auto-run detection after loading
        runDetection()
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

    fun runDetection() {
        val state = _uiState.value
        if (state.editedLines.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val editedOcr = OcrResult(
                    lines = state.editedLines.map { text ->
                        OcrLine(text = text, boundingBox = null, confidence = null)
                    }
                )
                val result = parserFactory.parse(editedOcr, state.parserTypeOverride)
                _uiState.update {
                    it.copy(
                        parsedReceipt = result,
                        detectedParserType = result.detectedParserType,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal memproses: ${e.message}"
                    )
                }
            }
        }
    }

    fun setParserTypeOverride(type: ParserType?) {
        _uiState.update { it.copy(parserTypeOverride = type) }
        runDetection()
    }

    /**
     * Apply parsed receipt and navigate back.
     * The parsed receipt is exposed via [uiState] so the calling screen
     * can pass it to AddEditViewModel.applyParsedReceipt().
     */
    fun applyToForm(onApplied: (ParsedReceipt) -> Unit) {
        val parsed = _uiState.value.parsedReceipt
        if (parsed != null) {
            onApplied(parsed)
        }
    }
}
