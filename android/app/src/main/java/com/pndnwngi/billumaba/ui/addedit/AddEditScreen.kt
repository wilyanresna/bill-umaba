package com.pndnwngi.billumaba.ui.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.pndnwngi.billumaba.ui.components.PhotoPicker
import com.pndnwngi.billumaba.ui.components.StarRating
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Navigation back on save success
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.visitId == null) "Tambah Catatan" else "Edit Catatan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveVisit() },
                        enabled = !uiState.isSaving && !uiState.isLoading
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Simpan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Photo Picker Section
                    PhotoPicker(
                        selectedPhotoPath = uiState.receiptPhotoPath,
                        onPhotoSelected = { uri ->
                            viewModel.onPhotoSelected(uri.toString())
                        }
                    )

                    // 2. Restaurant details card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Informasi Tempat Kuliner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Restaurant Name
                            OutlinedTextField(
                                value = uiState.restaurantName,
                                onValueChange = { viewModel.onRestaurantNameChange(it) },
                                label = { Text("Nama Tempat *") },
                                placeholder = { Text("Contoh: Bakso Pak Kumis") },
                                isError = uiState.restaurantNameError != null,
                                supportingText = uiState.restaurantNameError?.let { { Text(it) } },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Restaurant Address
                            OutlinedTextField(
                                value = uiState.restaurantAddress,
                                onValueChange = { viewModel.onRestaurantAddressChange(it) },
                                label = { Text("Alamat Tempat") },
                                placeholder = { Text("Contoh: Jl. Diponegoro No. 12") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Date Picker Trigger Field
                            OutlinedTextField(
                                value = formatDate(uiState.visitDate),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tanggal Kunjungan") },
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Pilih Tanggal"
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Restaurant Rating
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Rating Tempat: ${uiState.restaurantRating}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                StarRating(
                                    rating = uiState.restaurantRating,
                                    onRatingChange = { viewModel.onRestaurantRatingChange(it) },
                                    starSize = 32.dp
                                )
                            }

                            // Restaurant Review
                            OutlinedTextField(
                                value = uiState.restaurantReview,
                                onValueChange = { viewModel.onRestaurantReviewChange(it) },
                                label = { Text("Ulasan Singkat") },
                                placeholder = { Text("Bagaimana suasana dan rasanya secara umum?") },
                                shape = RoundedCornerShape(12.dp),
                                minLines = 2,
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // 3. Menu items section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Menu Yang Dipesan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { viewModel.onAddMenuItem() },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Tambah Menu", fontSize = 12.sp)
                            }
                        }

                        if (uiState.menuItemsError != null) {
                            Text(
                                text = uiState.menuItemsError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        uiState.menuItems.forEachIndexed { index, item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Row 1: Name and Delete button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = item.name,
                                            onValueChange = {
                                                viewModel.onMenuItemChange(index, item.copy(name = it))
                                            },
                                            label = { Text("Nama Menu *") },
                                            placeholder = { Text("Contoh: Nasi Goreng") },
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = { viewModel.onRemoveMenuItem(index) },
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Menu",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }

                                    // Row 2: Quantity, Price, Subtotal
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Quantity input
                                        OutlinedTextField(
                                            value = if (item.quantity == 0) "" else item.quantity.toString(),
                                            onValueChange = { qtyStr ->
                                                val clean = qtyStr.filter { it.isDigit() }
                                                val qty = if (clean.isEmpty()) 0 else clean.toIntOrNull() ?: 1
                                                viewModel.onMenuItemChange(index, item.copy(quantity = qty))
                                            },
                                            label = { Text("Porsi") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            modifier = Modifier.width(70.dp)
                                        )

                                        // Price input
                                        OutlinedTextField(
                                            value = if (item.price == 0.0) "" else item.price.toLong().toString(),
                                            onValueChange = { priceStr ->
                                                val clean = priceStr.filter { it.isDigit() }
                                                val price = if (clean.isEmpty()) 0.0 else clean.toDoubleOrNull() ?: 0.0
                                                viewModel.onMenuItemChange(index, item.copy(price = price))
                                            },
                                            label = { Text("Harga Satuan") },
                                            prefix = { Text("Rp ") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Subtotal Preview
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier.width(100.dp)
                                        ) {
                                            Text(
                                                text = "Subtotal",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = formatRupiah(item.quantity * item.price),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    // Row 3: Star rating and notes
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.width(130.dp)) {
                                            Text(
                                                text = "Rating Menu: ${item.rating}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            StarRating(
                                                rating = item.rating,
                                                onRatingChange = {
                                                    viewModel.onMenuItemChange(index, item.copy(rating = it))
                                                },
                                                starSize = 20.dp
                                            )
                                        }

                                        OutlinedTextField(
                                            value = item.notes,
                                            onValueChange = {
                                                viewModel.onMenuItemChange(index, item.copy(notes = it))
                                            },
                                            label = { Text("Catatan Rasa/Porsi") },
                                            placeholder = { Text("Pedas, manis, porsi banyak") },
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Grand total override section
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Total Pembayaran",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            val calculatedSum = uiState.menuItems.sumOf { it.quantity * it.price }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Menu (Subtotal):", fontSize = 14.sp)
                                Text(
                                    text = formatRupiah(calculatedSum),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Kustomisasi Total (Pajak/Tips/Diskon)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Centang jika total di struk berbeda dengan jumlah item",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.isGrandTotalOverridden,
                                    onCheckedChange = { viewModel.onGrandTotalOverrideChange(it) }
                                )
                            }

                            if (uiState.isGrandTotalOverridden) {
                                OutlinedTextField(
                                    value = if (uiState.manualGrandTotal == 0.0) "" else uiState.manualGrandTotal.toLong().toString(),
                                    onValueChange = { totalStr ->
                                        val clean = totalStr.filter { it.isDigit() }
                                        val total = if (clean.isEmpty()) 0.0 else clean.toDoubleOrNull() ?: 0.0
                                        viewModel.onManualGrandTotalChange(total)
                                    },
                                    label = { Text("Total Pembayaran Manual") },
                                    prefix = { Text("Rp ") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Grand Total Akhir:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = formatRupiah(calculatedSum),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Spacer bottom
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // Saving loader overlay
            if (uiState.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sedang mengompres foto dan menyimpan...",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Error display snackbar/dialog
            uiState.errorMessage?.let { errorMsg ->
                AlertDialog(
                    onDismissRequest = { /* Custom dismiss or handle in VM */ },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onRestaurantNameChange(uiState.restaurantName) /* triggers state refresh */ }) {
                            Text("OK")
                        }
                    },
                    title = { Text("Gagal Menyimpan") },
                    text = { Text(errorMsg) }
                )
            }
        }
    }

    // Material 3 Date Picker Dialog
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
                            viewModel.onVisitDateChange(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
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

private fun formatRupiah(amount: Double): String {
    val intVal = amount.toLong()
    val formatted = String.format("%,d", intVal).replace(',', '.')
    return "Rp $formatted"
}

private fun formatDate(epochMillis: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        sdf.format(Date(epochMillis))
    } catch (e: Exception) {
        ""
    }
}
