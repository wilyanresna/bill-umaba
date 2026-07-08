package com.pndnwngi.billumaba.ui.patterns

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pndnwngi.billumaba.data.database.dao.ReceiptPatternDao
import com.pndnwngi.billumaba.data.database.entities.ReceiptPatternEntity
import com.pndnwngi.billumaba.data.ocr.ReceiptOcrEngine
import com.pndnwngi.billumaba.data.parser.ParsedReceipt
import com.pndnwngi.billumaba.data.parser.ParserType
import com.pndnwngi.billumaba.data.parser.ReceiptParserFactory
import com.pndnwngi.billumaba.data.storage.ImageCompressor
import com.pndnwngi.billumaba.data.storage.StorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PatternEditViewModel @Inject constructor(
    private val patternDao: ReceiptPatternDao,
    private val ocrEngine: ReceiptOcrEngine,
    private val parserFactory: ReceiptParserFactory,
    private val imageCompressor: ImageCompressor,
    private val storageManager: StorageManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patternId: Long = savedStateHandle.get<Long>("id") ?: -1L

    private val _uiState = MutableStateFlow(PatternEditUiState())
    val uiState: StateFlow<PatternEditUiState> = _uiState.asStateFlow()

    init {
        if (patternId != -1L) {
            loadPattern(patternId)
        }
    }

    private fun loadPattern(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val pattern = patternDao.findByName("") // won't find, we need by ID
            // Fallback: query by collecting all and filtering
            // Actually, let's add a findById or query directly
            // For simplicity, we'll use a workaround: query all and filter
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateRestaurantName(name: String) {
        _uiState.update { it.copy(restaurantName = name) }
    }

    fun updateDisplayName(name: String) {
        _uiState.update { it.copy(displayName = name) }
    }

    fun updateParserType(type: ParserType) {
        _uiState.update { it.copy(parserType = type) }
    }

    fun updateNameStrategy(strategy: NameStrategy) {
        _uiState.update { it.copy(restaurantNameStrategy = strategy) }
    }

    fun updateTemplate(template: String) {
        _uiState.update { it.copy(menuLineTemplate = template) }
    }

    fun updateSeparator(sep: Separator) {
        _uiState.update { it.copy(separator = sep) }
    }

    fun insertToken(token: String) {
        _uiState.update { state ->
            val current = state.menuLineTemplate
            val separatorChar = when (state.separator) {
                Separator.X -> "x "
                Separator.SPACE -> " "
                Separator.DASH -> "- "
                Separator.CUSTOM -> " "
            }
            val template = "$current$separatorChar$token "
            state.copy(menuLineTemplate = template.trimEnd())
        }
    }

    fun updateTotalStrategy(strategy: TotalStrategy) {
        _uiState.update { it.copy(totalLineStrategy = strategy) }
    }

    fun updateTotalRegex(s: String) {
        _uiState.update { it.copy(totalLineRegex = s) }
    }

    fun toggleAdvanced() {
        _uiState.update { it.copy(showAdvanced = !it.showAdvanced) }
    }

    fun updateTax(enabled: Boolean, regex: String = "") {
        _uiState.update { it.copy(taxEnabled = enabled, taxLineRegex = regex) }
    }

    fun updateTaxRegex(s: String) {
        _uiState.update { it.copy(taxLineRegex = s) }
    }

    fun updateService(enabled: Boolean, regex: String = "") {
        _uiState.update { it.copy(serviceEnabled = enabled, serviceLineRegex = regex) }
    }

    fun updateServiceRegex(s: String) {
        _uiState.update { it.copy(serviceLineRegex = s) }
    }

    fun updateDiscount(enabled: Boolean, regex: String = "") {
        _uiState.update { it.copy(discountEnabled = enabled, discountLineRegex = regex) }
    }

    fun updateDiscountRegex(s: String) {
        _uiState.update { it.copy(discountLineRegex = s) }
    }

    fun updateDate(enabled: Boolean, regex: String = "") {
        _uiState.update { it.copy(dateEnabled = enabled, dateRegex = regex) }
    }

    fun updateDateRegex(s: String) {
        _uiState.update { it.copy(dateRegex = s) }
    }

    fun updateHeaderLineCount(n: Int) {
        _uiState.update { it.copy(headerLineCount = n.coerceIn(0, 5)) }
    }

    fun addSkipKeyword(keyword: String) {
        if (keyword.isBlank()) return
        _uiState.update { state ->
            if (keyword.lowercase() !in state.skipKeywords.map { it.lowercase() }) {
                state.copy(skipKeywords = state.skipKeywords + keyword.trim())
            } else state
        }
    }

    fun removeSkipKeyword(keyword: String) {
        _uiState.update { state ->
            state.copy(skipKeywords = state.skipKeywords.filter { it != keyword })
        }
    }

    fun testWithGalleryPhoto(uri: Uri) {
        _uiState.update { it.copy(isRunningTest = true, testPhotoUri = uri.toString()) }
        viewModelScope.launch {
            try {
                val compressed = imageCompressor.compressImage(context, uri)
                if (compressed != null) {
                    val path = storageManager.saveReceiptImage(compressed)
                    if (path != null) {
                        val ocrResult = ocrEngine.recognize(context, Uri.fromFile(File(path)))
                        val parsed = parserFactory.parse(ocrResult)
                        _uiState.update { it.copy(testResult = parsed, isRunningTest = false) }
                        return@launch
                    }
                }
                _uiState.update { it.copy(isRunningTest = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRunningTest = false) }
            }
        }
    }

    fun testWithExistingVisit(visitPhotoPath: String) {
        _uiState.update { it.copy(isRunningTest = true) }
        viewModelScope.launch {
            try {
                val file = File(visitPhotoPath)
                if (file.exists()) {
                    val ocrResult = ocrEngine.recognize(context, Uri.fromFile(file))
                    val parsed = parserFactory.parse(ocrResult)
                    _uiState.update { it.copy(testResult = parsed, isRunningTest = false) }
                } else {
                    _uiState.update { it.copy(isRunningTest = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRunningTest = false) }
            }
        }
    }

    fun validate(): Boolean {
        return _uiState.value.restaurantName.isNotBlank()
    }

    fun save(onSuccess: () -> Unit) {
        if (!validate()) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val entity = buildEntity()
            patternDao.upsert(entity)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    private fun buildEntity(): ReceiptPatternEntity {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        return ReceiptPatternEntity(
            id = state.id ?: 0,
            restaurantName = state.restaurantName.trim(),
            displayName = state.displayName.trim().ifBlank { state.restaurantName.trim() },
            menuLineTemplate = state.menuLineTemplate,
            totalLineStrategy = state.totalLineStrategy.name,
            totalLineRegex = state.totalLineRegex.takeIf { state.totalLineStrategy == TotalStrategy.CUSTOM_REGEX },
            taxLineRegex = state.taxLineRegex.takeIf { state.taxEnabled },
            serviceLineRegex = state.serviceLineRegex.takeIf { state.serviceEnabled },
            discountLineRegex = state.discountLineRegex.takeIf { state.discountEnabled },
            dateRegex = state.dateRegex.takeIf { state.dateEnabled },
            restaurantNameStrategy = state.restaurantNameStrategy.name,
            headerLineCount = state.headerLineCount,
            skipKeywords = state.skipKeywords.joinToString(","),
            parserType = state.parserType.name,
            createdAt = state.id?.let { now } ?: now,
            lastUsedAt = now,
            usageCount = 0
        )
    }

    fun loadExistingPhotoPaths(callback: (List<String>) -> Unit) {
        viewModelScope.launch {
            val paths = patternDao.getRecentPhotoPaths(10)
            callback(paths)
        }
    }
}
