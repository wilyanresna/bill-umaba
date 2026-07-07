package com.pndnwngi.billumaba.ui.detail

import com.pndnwngi.billumaba.data.database.entities.VisitWithMenus

data class DetailUiState(
    val isLoading: Boolean = false,
    val visitWithMenus: VisitWithMenus? = null,
    val errorMessage: String? = null,
    val isDeleteSuccess: Boolean = false,
    val isDeleting: Boolean = false
)
