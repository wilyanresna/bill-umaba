package com.pndnwngi.billumaba.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pndnwngi.billumaba.data.database.entities.MenuItemEntity
import com.pndnwngi.billumaba.data.database.entities.VisitEntity
import com.pndnwngi.billumaba.data.repository.CulinaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: CulinaryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val visitId: Long = savedStateHandle.get<Long>("visitId") ?: -1L

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        if (visitId != -1L) {
            loadVisit(visitId)
        }
    }

    private fun loadVisit(id: Long) {
        viewModelScope.launch {
            val visitWithMenus = repository.getVisitWithMenusById(id).firstOrNull()
            visitWithMenus?.let { data ->
                _uiState.update { state ->
                    state.copy(
                        visitId = data.visit.id,
                        isEditMode = true,
                        existingPhotoPath = data.visit.receiptPhotoPath,
                        restaurantName = data.visit.restaurantName,
                        restaurantAddress = data.visit.restaurantAddress ?: "",
                        restaurantRating = data.visit.restaurantRating,
                        restaurantReview = data.visit.restaurantReview ?: "",
                        visitDate = data.visit.visitDate,
                        menuItems = data.menuItems.map { item ->
                            MenuItemInput(
                                name = item.name,
                                quantity = item.quantity.toString(),
                                price = item.price.toBigDecimal().toPlainString(),
                                rating = item.rating,
                                notes = item.notes ?: ""
                            )
                        }
                    )
                }
            }
        }
    }

    fun onRestaurantNameChanged(name: String) {
        _uiState.update { it.copy(restaurantName = name, restaurantNameError = false) }
    }

    fun onRestaurantAddressChanged(address: String) {
        _uiState.update { it.copy(restaurantAddress = address) }
    }

    fun onRestaurantRatingChanged(rating: Float) {
        _uiState.update { it.copy(restaurantRating = rating) }
    }

    fun onRestaurantReviewChanged(review: String) {
        _uiState.update { it.copy(restaurantReview = review) }
    }

    fun onVisitDateChanged(date: Long) {
        _uiState.update { it.copy(visitDate = date) }
    }

    fun onPhotoSelected(uri: String) {
        _uiState.update { it.copy(receiptPhotoUri = uri) }
    }

    fun onPhotoRemoved() {
        _uiState.update { it.copy(receiptPhotoUri = null, existingPhotoPath = null) }
    }

    fun onMenuItemNameChanged(index: Int, name: String) {
        updateMenuItem(index) { it.copy(name = name) }
        _uiState.update { it.copy(menuItemsError = false) }
    }

    fun onMenuItemQuantityChanged(index: Int, quantity: String) {
        updateMenuItem(index) { it.copy(quantity = quantity) }
    }

    fun onMenuItemPriceChanged(index: Int, price: String) {
        updateMenuItem(index) { it.copy(price = price) }
    }

    fun onMenuItemRatingChanged(index: Int, rating: Float) {
        updateMenuItem(index) { it.copy(rating = rating) }
    }

    fun onMenuItemNotesChanged(index: Int, notes: String) {
        updateMenuItem(index) { it.copy(notes = notes) }
    }

    fun addMenuItem() {
        _uiState.update { state ->
            state.copy(
                menuItems = state.menuItems + MenuItemInput(),
                menuItemsError = false
            )
        }
    }

    fun removeMenuItem(index: Int) {
        _uiState.update { state ->
            if (state.menuItems.size > 1) {
                state.copy(menuItems = state.menuItems.toMutableList().apply { removeAt(index) })
            } else {
                state
            }
        }
    }

    fun onGrandTotalOverrideChanged(value: String) {
        _uiState.update { it.copy(grandTotalOverride = value, isGrandTotalOverridden = true) }
    }

    fun onResetGrandTotalOverride() {
        _uiState.update { it.copy(grandTotalOverride = "", isGrandTotalOverridden = false) }
    }

    private fun updateMenuItem(index: Int, transform: (MenuItemInput) -> MenuItemInput) {
        _uiState.update { state ->
            val updatedList = state.menuItems.toMutableList()
            if (index in updatedList.indices) {
                updatedList[index] = transform(updatedList[index])
            }
            state.copy(menuItems = updatedList)
        }
    }

    fun save() {
        val state = _uiState.value

        var hasError = false
        if (state.restaurantName.isBlank()) {
            _uiState.update { it.copy(restaurantNameError = true) }
            hasError = true
        }
        if (state.menuItems.none { it.name.isNotBlank() }) {
            _uiState.update { it.copy(menuItemsError = true) }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val visit = VisitEntity(
                    id = if (state.isEditMode) state.visitId else 0,
                    restaurantName = state.restaurantName.trim(),
                    restaurantAddress = state.restaurantAddress.trim().ifBlank { null },
                    restaurantRating = state.restaurantRating,
                    restaurantReview = state.restaurantReview.trim().ifBlank { null },
                    visitDate = state.visitDate,
                    receiptPhotoPath = state.existingPhotoPath,
                    grandTotal = state.displayGrandTotal
                )

                val menuEntities = state.menuItems
                    .filter { it.name.isNotBlank() }
                    .map { input ->
                        MenuItemEntity(
                            visitId = if (state.isEditMode) state.visitId else 0,
                            name = input.name.trim(),
                            quantity = input.quantity.toIntOrNull() ?: 1,
                            price = input.price.toDoubleOrNull() ?: 0.0,
                            rating = input.rating,
                            notes = input.notes.trim().ifBlank { null }
                        )
                    }

                repository.saveVisit(
                    visit = visit,
                    menuItems = menuEntities,
                    newPhotoUri = state.receiptPhotoUri,
                    deleteOldPhoto = state.isEditMode && state.existingPhotoPath == null && state.receiptPhotoUri == null
                )

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
