package com.pndnwngi.billumaba.ui.addedit

data class MenuItemInput(
    val id: Long = 0,
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val rating: Float = 5.0f,
    val notes: String = ""
)

data class AddEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val visitId: Long? = null,
    val restaurantName: String = "",
    val restaurantAddress: String = "",
    val restaurantRating: Float = 5.0f,
    val restaurantReview: String = "",
    val visitDate: Long = System.currentTimeMillis(),
    val receiptPhotoPath: String? = null,
    val menuItems: List<MenuItemInput> = listOf(MenuItemInput()), // Start with 1 empty menu item
    val isGrandTotalOverridden: Boolean = false,
    val manualGrandTotal: Double = 0.0,
    val errorMessage: String? = null,
    
    // Field errors
    val restaurantNameError: String? = null,
    val menuItemsError: String? = null
)
