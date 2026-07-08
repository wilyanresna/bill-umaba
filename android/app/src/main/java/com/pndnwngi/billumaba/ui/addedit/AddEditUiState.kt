package com.pndnwngi.billumaba.ui.addedit

import com.pndnwngi.billumaba.data.ocr.OcrResult

data class MenuItemInput(
    val name: String = "",
    val quantity: String = "1",
    val price: String = "",
    val rating: Float = 5.0f,
    val notes: String = ""
) {
    val subtotal: Double
        get() {
            val q = quantity.toIntOrNull() ?: 0
            val p = price.toDoubleOrNull() ?: 0.0
            return q * p
        }
}

data class AddEditUiState(
    val visitId: Long = -1L,
    val isEditMode: Boolean = false,
    val receiptPhotoUri: String? = null,
    val existingPhotoPath: String? = null,
    val restaurantName: String = "",
    val restaurantAddress: String = "",
    val restaurantRating: Float = 5.0f,
    val restaurantReview: String = "",
    val visitDate: Long = System.currentTimeMillis(),
    val menuItems: List<MenuItemInput> = listOf(MenuItemInput()),
    val grandTotalOverride: String = "",
    val isGrandTotalOverridden: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val restaurantNameError: Boolean = false,
    val menuItemsError: Boolean = false,
    val isProcessingScan: Boolean = false,
    val pendingGalleryUri: String? = null,
    val showRapikanDialog: Boolean = false,
    val showGmsFallbackDialog: Boolean = false,
    val isRunningOcr: Boolean = false,
    val ocrResult: OcrResult? = null
) {
    val calculatedGrandTotal: Double
        get() = menuItems.sumOf { it.subtotal }

    val displayGrandTotal: Double
        get() = if (isGrandTotalOverridden) {
            grandTotalOverride.toDoubleOrNull() ?: calculatedGrandTotal
        } else {
            calculatedGrandTotal
        }
}
