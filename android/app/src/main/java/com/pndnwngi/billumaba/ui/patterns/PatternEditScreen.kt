package com.pndnwngi.billumaba.ui.patterns

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pndnwngi.billumaba.data.parser.ParserType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PatternEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: PatternEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var skipKeywordInput by remember { mutableStateOf("") }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.testWithGalleryPhoto(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.id != null) "Edit Pattern" else "Tambah Pattern") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save(onSuccess = onNavigateBack) },
                        enabled = !uiState.isSaving && uiState.restaurantName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Simpan")
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
            // 1. Basic Info
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Informasi Dasar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = uiState.restaurantName,
                        onValueChange = viewModel::updateRestaurantName,
                        label = { Text("Nama Restoran *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.displayName,
                        onValueChange = viewModel::updateDisplayName,
                        label = { Text("Nama Tampilan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    ParserTypeDropdown(
                        selected = uiState.parserType,
                        onSelect = viewModel::updateParserType
                    )
                }
            }

            // 2. Restaurant Name Strategy
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sumber Nama Restoran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    NameStrategy.entries.forEach { strategy ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = uiState.restaurantNameStrategy == strategy,
                                onClick = { viewModel.updateNameStrategy(strategy) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strategy.displayName)
                        }
                    }
                }
            }

            // 3. Menu Line Template
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Format Item Menu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Tap token untuk insert ke template", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = { viewModel.insertToken("{qty}") }, label = { Text("QTY") })
                        AssistChip(onClick = { viewModel.insertToken("{name}") }, label = { Text("NAMA") })
                        AssistChip(onClick = { viewModel.insertToken("{price}") }, label = { Text("HARGA") })
                        AssistChip(onClick = { viewModel.insertToken("{subtotal}") }, label = { Text("SUBTOTAL") })
                    }
                    OutlinedTextField(
                        value = uiState.menuLineTemplate,
                        onValueChange = viewModel::updateTemplate,
                        label = { Text("Template") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    SeparatorDropdown(
                        selected = uiState.separator,
                        onSelect = viewModel::updateSeparator
                    )
                }
            }

            // 4. Grand Total Strategy
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Strategi Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TotalStrategy.entries.forEach { strategy ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = uiState.totalLineStrategy == strategy,
                                onClick = { viewModel.updateTotalStrategy(strategy) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strategy.displayName)
                        }
                    }
                    if (uiState.totalLineStrategy == TotalStrategy.CUSTOM_REGEX) {
                        OutlinedTextField(
                            value = uiState.totalLineRegex,
                            onValueChange = viewModel::updateTotalRegex,
                            label = { Text("Regex Total") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // 5. Optional Fields (Tax, Service, Discount, Date)
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Field Opsional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OptionalRegexRow(label = "Tax/PPN", enabled = uiState.taxEnabled, regex = uiState.taxLineRegex, onToggle = { viewModel.updateTax(it) }, onRegexChange = viewModel::updateTaxRegex)
                    OptionalRegexRow(label = "Service", enabled = uiState.serviceEnabled, regex = uiState.serviceLineRegex, onToggle = { viewModel.updateService(it) }, onRegexChange = viewModel::updateServiceRegex)
                    OptionalRegexRow(label = "Discount", enabled = uiState.discountEnabled, regex = uiState.discountLineRegex, onToggle = { viewModel.updateDiscount(it) }, onRegexChange = viewModel::updateDiscountRegex)
                    OptionalRegexRow(label = "Tanggal", enabled = uiState.dateEnabled, regex = uiState.dateRegex, onToggle = { viewModel.updateDate(it) }, onRegexChange = viewModel::updateDateRegex)
                }
            }

            // 6. Skip Keywords
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Skip Keywords", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Baris yang mengandung keyword ini akan di-skip", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.skipKeywords.forEach { keyword ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.removeSkipKeyword(keyword) },
                                label = { Text(keyword) }
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = skipKeywordInput,
                            onValueChange = { skipKeywordInput = it },
                            label = { Text("Keyword baru") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                viewModel.addSkipKeyword(skipKeywordInput)
                                skipKeywordInput = ""
                            },
                            enabled = skipKeywordInput.isNotBlank()
                        ) {
                            Text("Tambah")
                        }
                    }
                    // Suggestions
                    Text("Contoh:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("subtotal", "pajak", "diskon", "service").forEach { suggestion ->
                            AssistChip(
                                onClick = { viewModel.addSkipKeyword(suggestion) },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }
            }

            // 7. Header Lines
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Header Lines to Skip", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.headerLineCount.toString(),
                        onValueChange = { viewModel.updateHeaderLineCount(it.toIntOrNull() ?: 2) },
                        label = { Text("Jumlah baris header") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // 8. Test Section
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test dengan foto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        androidx.compose.material3.OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Text("Pilih dari Galeri")
                        }
                        androidx.compose.material3.OutlinedButton(onClick = {
                            viewModel.loadExistingPhotoPaths { paths ->
                                if (paths.isNotEmpty()) {
                                    viewModel.testWithExistingVisit(paths.first())
                                }
                            }
                        }) {
                            Text("Foto Kunjungan")
                        }
                    }
                    if (uiState.isRunningTest) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.height(20.dp).width(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Memproses...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    uiState.testResult?.let { result ->
                        Text(
                            text = "Terdeteksi: ${result.itemCount} item, Total Rp ${formatPrice(result.grandTotal ?: 0.0)}, Resto: ${result.restaurantName ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 9. Advanced Section
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextButton(onClick = viewModel::toggleAdvanced) {
                        Text(if (uiState.showAdvanced) "Sembunyikan Regex" else "Advanced: lihat raw regex")
                    }
                    if (uiState.showAdvanced) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Raw Regex Menu:", style = MaterialTheme.typography.labelMedium)
                        val menuRegex = try {
                            com.pndnwngi.billumaba.data.parser.TemplateToRegex.convert(uiState.menuLineTemplate).pattern
                        } catch (e: Exception) {
                            "Invalid template"
                        }
                        Text(
                            text = menuRegex,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 10. Save Button
            androidx.compose.material3.Button(
                onClick = { viewModel.save(onSuccess = onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && uiState.restaurantName.isNotBlank()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp).width(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Simpan Pattern")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParserTypeDropdown(selected: ParserType, onSelect: (ParserType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipe Parser") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ParserType.entries.forEach { type ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = { onSelect(type); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeparatorDropdown(selected: Separator, onSelect: (Separator) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Separator") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Separator.entries.forEach { sep ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(sep.displayName) },
                    onClick = { onSelect(sep); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun OptionalRegexRow(
    label: String,
    enabled: Boolean,
    regex: String,
    onToggle: (Boolean) -> Unit,
    onRegexChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Switch(checked = enabled, onCheckedChange = onToggle)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (enabled) {
                OutlinedTextField(
                    value = regex,
                    onValueChange = onRegexChange,
                    label = { Text("Regex $label") },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    singleLine = true
                )
            }
        }
    }
}

private fun formatPrice(value: Double): String {
    val formatted = "%,.0f".format(value).replace(",", ".")
    return formatted
}
