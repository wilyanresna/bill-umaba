package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.ocr.OcrResult

data class ParsedReceipt(
    val detectedParserType: ParserType,
    val restaurantName: String? = null,
    val menuItems: List<ParsedMenuItem> = emptyList(),
    val grandTotal: Double? = null,
    val tax: Double? = null,
    val service: Double? = null,
    val discount: Double? = null,
    val visitDate: Long? = null
)

enum class ParserType { GENERAL, RESTAURANT, RETAIL_THERMAL }

data class ParsedMenuItem(
    val name: String,
    val quantity: Int = 1,
    val price: Double,
    val subtotal: Double
)

interface ReceiptParser {
    fun parse(ocr: OcrResult): ParsedReceipt
}
