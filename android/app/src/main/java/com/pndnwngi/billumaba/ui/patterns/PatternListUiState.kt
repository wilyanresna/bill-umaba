package com.pndnwngi.billumaba.ui.patterns

import com.pndnwngi.billumaba.data.database.entities.ReceiptPatternEntity

data class PatternListUiState(
    val isLoading: Boolean = false,
    val patterns: List<ReceiptPatternEntity> = emptyList(),
    val pendingDeleteId: Long? = null
)
