package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.database.dao.ReceiptPatternDao
import com.pndnwngi.billumaba.data.ocr.OcrResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptParserFactory @Inject constructor(
    private val generalParser: GeneralReceiptParser,
    private val restaurantParser: RestaurantReceiptParser,
    private val retailParser: RetailThermalParser,
    private val patternDao: ReceiptPatternDao
) {

    suspend fun parse(
        ocr: OcrResult,
        restaurantName: String? = null,
        overrideType: ParserType? = null
    ): ParsedReceipt {
        // 1. Pattern lookup if restaurantName is provided
        if (!restaurantName.isNullOrBlank()) {
            val pattern = patternDao.findByName(restaurantName)
            if (pattern != null) {
                patternDao.touch(pattern.id, System.currentTimeMillis())
                return PatternReceiptParser(pattern).parse(ocr)
            }
        }
        // 2. Auto-detect or override
        val type = overrideType ?: autoDetectType(ocr)
        val parser = when (type) {
            ParserType.GENERAL -> generalParser
            ParserType.RESTAURANT -> restaurantParser
            ParserType.RETAIL_THERMAL -> retailParser
        }
        return parser.parse(ocr)
    }

    fun autoDetectType(ocr: OcrResult): ParserType {
        val text = ocr.lines.joinToString("\n") { it.text.lowercase() }

        // Retail markers: cash register keywords
        val hasCashMarkers = listOf("tunai", "kembali", "kembalian", "bayar")
            .any { text.contains(it) }
        if (hasCashMarkers) return ParserType.RETAIL_THERMAL

        // Restaurant markers: subtotal + tax/service
        val hasSubtotal = text.contains("subtotal") || text.contains("sub total")
        val hasTaxOrService = text.contains("pajak") || text.contains("ppn") ||
                text.contains("service") || text.contains("pb1")
        if (hasSubtotal && hasTaxOrService) return ParserType.RESTAURANT

        // Default: general
        return ParserType.GENERAL
    }
}
