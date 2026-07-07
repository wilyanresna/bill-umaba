package com.pndnwngi.billumaba.ui.detail

import com.pndnwngi.billumaba.data.database.entities.MenuItemEntity
import com.pndnwngi.billumaba.data.database.entities.VisitEntity

data class DetailUiState(
    val visit: VisitEntity? = null,
    val menuItems: List<MenuItemEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
)
