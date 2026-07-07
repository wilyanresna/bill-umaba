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
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: CulinaryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        val visitIdStr = savedStateHandle.get<String>("visitId")
        val id = visitIdStr?.toLongOrNull()
        if (id != null) {
            loadVisit(id)
        }
    }

    private fun loadVisit(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getVisitWithMenusById(id).take(1).collect { visitWithMenus ->
                if (visitWithMenus != null) {
                    _uiState.value = AddEditUiState(
                        visitId = id,
                        restaurantName = visitWithMenus.visit.restaurantName,
                        restaurantAddress = visitWithMenus.visit.restaurantAddress ?: "",
                        restaurantRating = visitWithMenus.visit.restaurantRating,
                        restaurantReview = visitWithMenus.visit.restaurantReview ?: "",
                        visitDate = visitWithMenus.visit.visitDate,
                        receiptPhotoPath = visitWithMenus.visit.receiptPhotoPath,
                        menuItems = visitWithMenus.menuItems.map {
                            MenuItemInput(
                                id = it.id,
                                name = it.name,
                                quantity = it.quantity,
                                price = it.price,
                                rating = it.rating,
                                notes = it.notes ?: ""
                            )
                        },
                        isGrandTotalOverridden = visitWithMenus.visit.grandTotal != visitWithMenus.menuItems.sumOf { it.quantity * it.price },
                        manualGrandTotal = visitWithMenus.visit.grandTotal,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Kunjungan tidak ditemukan"
                    )
                }
            }
        }
    }

    fun onRestaurantNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            restaurantName = name,
            restaurantNameError = if (name.isBlank()) "Nama tempat wajib diisi" else null
        )
    }

    fun onRestaurantAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(restaurantAddress = address)
    }

    fun onRestaurantRatingChange(rating: Float) {
        _uiState.value = _uiState.value.copy(restaurantRating = rating)
    }

    fun onRestaurantReviewChange(review: String) {
        _uiState.value = _uiState.value.copy(restaurantReview = review)
    }

    fun onVisitDateChange(dateMillis: Long) {
        _uiState.value = _uiState.value.copy(visitDate = dateMillis)
    }

    fun onPhotoSelected(uriString: String) {
        _uiState.value = _uiState.value.copy(receiptPhotoPath = uriString)
    }

    fun onAddMenuItem() {
        val currentItems = _uiState.value.menuItems.toMutableList()
        currentItems.add(MenuItemInput())
        _uiState.value = _uiState.value.copy(
            menuItems = currentItems,
            menuItemsError = null
        )
    }

    fun onRemoveMenuItem(index: Int) {
        val currentItems = _uiState.value.menuItems.toMutableList()
        if (currentItems.size > index) {
            currentItems.removeAt(index)
        }
        // If empty, add a default empty one or let validation handle it
        if (currentItems.isEmpty()) {
            currentItems.add(MenuItemInput())
        }
        _uiState.value = _uiState.value.copy(menuItems = currentItems)
    }

    fun onMenuItemChange(index: Int, updatedItem: MenuItemInput) {
        val currentItems = _uiState.value.menuItems.toMutableList()
        if (currentItems.size > index) {
            currentItems[index] = updatedItem
        }
        _uiState.value = _uiState.value.copy(
            menuItems = currentItems,
            menuItemsError = null
        )
    }

    fun onGrandTotalOverrideChange(overridden: Boolean) {
        val calculatedSum = _uiState.value.menuItems.sumOf { it.quantity * it.price }
        _uiState.value = _uiState.value.copy(
            isGrandTotalOverridden = overridden,
            manualGrandTotal = if (overridden) _uiState.value.manualGrandTotal else calculatedSum
        )
    }

    fun onManualGrandTotalChange(total: Double) {
        _uiState.value = _uiState.value.copy(manualGrandTotal = total)
    }

    fun saveVisit() {
        val currentState = _uiState.value
        val nameBlank = currentState.restaurantName.isBlank()
        
        // Validation: Minimal ada 1 menu item yang memiliki nama tidak kosong
        val validMenuItems = currentState.menuItems.filter { it.name.isNotBlank() }
        val menusInvalid = validMenuItems.isEmpty()

        if (nameBlank || menusInvalid) {
            _uiState.value = currentState.copy(
                restaurantNameError = if (nameBlank) "Nama tempat wajib diisi" else null,
                menuItemsError = if (menusInvalid) "Minimal harus ada 1 menu yang dipesan" else null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val grandTotal = if (currentState.isGrandTotalOverridden) {
                    currentState.manualGrandTotal
                } else {
                    validMenuItems.sumOf { it.quantity * it.price }
                }

                val visitEntity = VisitEntity(
                    id = currentState.visitId ?: 0L,
                    restaurantName = currentState.restaurantName,
                    restaurantAddress = currentState.restaurantAddress.ifBlank { null },
                    restaurantRating = currentState.restaurantRating,
                    restaurantReview = currentState.restaurantReview.ifBlank { null },
                    visitDate = currentState.visitDate,
                    receiptPhotoPath = currentState.receiptPhotoPath, // If it's a content URI, saveVisit will compress and rewrite
                    grandTotal = grandTotal
                )

                val menuEntities = validMenuItems.map {
                    MenuItemEntity(
                        id = it.id,
                        visitId = currentState.visitId ?: 0L,
                        name = it.name,
                        quantity = it.quantity,
                        price = it.price,
                        rating = it.rating,
                        notes = it.notes.ifBlank { null }
                    )
                }

                // Run saving (including photo compression) asynchronously using Dispatchers.Default/IO inside Repository and ViewModel
                withContext(Dispatchers.Default) {
                    // Check if photo is new (e.g. content:// uri or file:// uri)
                    val isNewPhoto = currentState.receiptPhotoPath?.startsWith("content://") == true ||
                            currentState.receiptPhotoPath?.startsWith("file://") == true
                    
                    repository.saveVisit(
                        visit = visitEntity,
                        menuItems = menuEntities,
                        newPhotoUri = if (isNewPhoto) currentState.receiptPhotoPath else null,
                        deleteOldPhoto = false // Handled inside saveVisit logic in implementation if replacing
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Gagal menyimpan catatan"
                )
            }
        }
    }
}
