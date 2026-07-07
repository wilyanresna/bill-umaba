package com.pndnwngi.billumaba.ui.addedit

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pndnwngi.billumaba.ui.components.PhotoPicker
import com.pndnwngi.billumaba.ui.components.StarRatingDisplay
import com.pndnwngi.billumaba.ui.components.StarRatingInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Edit Catatan" else "Tambah Catatan")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PhotoSection(
                photoUri = uiState.receiptPhotoUri,
                existingPhotoPath = uiState.existingPhotoPath,
                onPhotoSelected = viewModel::onPhotoSelected,
                onPhotoRemoved = viewModel::onPhotoRemoved
            )

            RestaurantInfoSection(
                name = uiState.restaurantName,
                address = uiState.restaurantAddress,
                rating = uiState.restaurantRating,
                review = uiState.restaurantReview,
                nameError = uiState.restaurantNameError,
                onNameChanged = viewModel::onRestaurantNameChanged,
                onAddressChanged = viewModel::onRestaurantAddressChanged,
                onRatingChanged = viewModel::onRestaurantRatingChanged,
                onReviewChanged = viewModel::onRestaurantReviewChanged
            )

            DateSection(
                date = uiState.visitDate,
                dateFormat = dateFormat,
                onDateClick = { showDatePicker = true }
            )

            MenuItemsSection(
                menuItems = uiState.menuItems,
                hasError = uiState.menuItemsError,
                onNameChanged = viewModel::onMenuItemNameChanged,
                onQuantityChanged = viewModel::onMenuItemQuantityChanged,
                onPriceChanged = viewModel::onMenuItemPriceChanged,
                onRatingChanged = viewModel::onMenuItemRatingChanged,
                onNotesChanged = viewModel::onMenuItemNotesChanged,
                onAddItem = viewModel::addMenuItem,
                onRemoveItem = viewModel::removeMenuItem
            )

            GrandTotalSection(
                calculatedTotal = uiState.calculatedGrandTotal,
                displayTotal = uiState.displayGrandTotal,
                overrideValue = uiState.grandTotalOverride,
                isOverridden = uiState.isGrandTotalOverridden,
                onOverrideChanged = viewModel::onGrandTotalOverrideChanged,
                onResetOverride = viewModel::onResetGrandTotalOverride
            )

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (uiState.isEditMode) "Simpan Perubahan" else "Simpan Catatan",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.visitDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onVisitDateChanged(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun PhotoSection(
    photoUri: String?,
    existingPhotoPath: String?,
    onPhotoSelected: (String) -> Unit,
    onPhotoRemoved: () -> Unit
) {
    val hasPhoto = photoUri != null || existingPhotoPath != null

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (hasPhoto) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Foto Struk",
                        style = MaterialTheme.typography.titleSmall
                    )
                    IconButton(onClick = onPhotoRemoved) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus Foto",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = photoUri ?: existingPhotoPath,
                    contentDescription = "Foto Struk",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                PhotoPicker(
                    onPhotoSelected = { uri -> onPhotoSelected(uri.toString()) }
                )
            }
        }
    }
}

@Composable
private fun RestaurantInfoSection(
    name: String,
    address: String,
    rating: Float,
    review: String,
    nameError: Boolean,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onRatingChanged: (Float) -> Unit,
    onReviewChanged: (String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Informasi Tempat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nama Tempat *") },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Nama tempat wajib diisi") }
                } else null,
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = onAddressChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Alamat") },
                singleLine = true
            )

            Column {
                Text(
                    text = "Rating Tempat: ${String.format("%.1f", rating)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                StarRatingInput(
                    rating = rating,
                    onRatingChanged = onRatingChanged,
                    starSize = 36.dp
                )
            }

            OutlinedTextField(
                value = review,
                onValueChange = onReviewChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ulasan Tempat") },
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun DateSection(
    date: Long,
    dateFormat: SimpleDateFormat,
    onDateClick: () -> Unit
) {
    OutlinedCard(
        onClick = onDateClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Tanggal",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Tanggal Kunjungan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(Date(date)),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MenuItemsSection(
    menuItems: List<MenuItemInput>,
    hasError: Boolean,
    onNameChanged: (Int, String) -> Unit,
    onQuantityChanged: (Int, String) -> Unit,
    onPriceChanged: (Int, String) -> Unit,
    onRatingChanged: (Int, Float) -> Unit,
    onNotesChanged: (Int, String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu yang Dipesan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onAddItem) {
                    Text("+ Tambah Menu")
                }
            }

            if (hasError) {
                Text(
                    text = "Minimal 1 menu wajib diisi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            menuItems.forEachIndexed { index, menuItem ->
                MenuItemCard(
                    index = index,
                    menuItem = menuItem,
                    canRemove = menuItems.size > 1,
                    onNameChanged = { onNameChanged(index, it) },
                    onQuantityChanged = { onQuantityChanged(index, it) },
                    onPriceChanged = { onPriceChanged(index, it) },
                    onRatingChanged = { onRatingChanged(index, it) },
                    onNotesChanged = { onNotesChanged(index, it) },
                    onRemove = { onRemoveItem(index) }
                )
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    index: Int,
    menuItem: MenuItemInput,
    canRemove: Boolean,
    onNameChanged: (String) -> Unit,
    onQuantityChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onRatingChanged: (Float) -> Unit,
    onNotesChanged: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu ${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Menu",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = menuItem.name,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nama Menu") },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = menuItem.quantity,
                    onValueChange = onQuantityChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("Jumlah") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = menuItem.price,
                    onValueChange = onPriceChanged,
                    modifier = Modifier.weight(2f),
                    label = { Text("Harga Satuan") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            Column {
                Text(
                    text = "Rating Menu: ${String.format("%.1f", menuItem.rating)}",
                    style = MaterialTheme.typography.bodySmall
                )
                StarRatingInput(
                    rating = menuItem.rating,
                    onRatingChanged = onRatingChanged,
                    starSize = 28.dp
                )
            }

            OutlinedTextField(
                value = menuItem.notes,
                onValueChange = onNotesChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Catatan Menu") },
                singleLine = true
            )

            Text(
                text = "Subtotal: ${formatRupiah(menuItem.subtotal)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GrandTotalSection(
    calculatedTotal: Double,
    displayTotal: Double,
    overrideValue: String,
    isOverridden: Boolean,
    onOverrideChanged: (String) -> Unit,
    onResetOverride: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Grand Total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Total dari menu: ${formatRupiah(calculatedTotal)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isOverridden) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = overrideValue,
                        onValueChange = onOverrideChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Override Total") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    TextButton(onClick = onResetOverride) {
                        Text("Reset")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { onOverrideChanged(displayTotal.toBigDecimal().toPlainString()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Override Grand Total (Pajak/Tips/Diskon)")
                }
            }

            Text(
                text = "Grand Total: ${formatRupiah(displayTotal)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatRupiah(amount: Double): String {
    val formatted = String.format("%,.0f", amount)
    return "Rp $formatted"
}
