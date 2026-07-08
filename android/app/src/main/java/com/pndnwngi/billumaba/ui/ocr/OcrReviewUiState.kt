package com.pndnwngi.billumaba.ui.ocr

import com.pndnwngi.billumaba.data.ocr.OcrResult
import com.pndnwngi.billumaba.data.parser.ParsedReceipt
import com.pndnwngi.billumaba.data.parser.ParserType

data class OcrReviewUiState(
    val isLoading: Boolean = false,
    val ocrResult: OcrResult? = null,
    val editedLines: List<String> = emptyList(),
    val errorMessage: String? = null,
    val parserTypeOverride: ParserType? = null,
    val detectedParserType: ParserType? = null,
    val parsedReceipt: ParsedReceipt? = null
)
