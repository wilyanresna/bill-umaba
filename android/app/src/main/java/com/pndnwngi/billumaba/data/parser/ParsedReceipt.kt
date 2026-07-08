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
) {
    val itemCount: Int get() = menuItems.size

    val summaryText: String
        get() {
            val parts = mutableListOf<String>()
            parts.add("$itemCount item")
            if (grandTotal != null) {
                parts.add("Total Rp ${formatPrice(grandTotal)}")
            }
            return parts.joinToString(", ")
        }

    private fun formatPrice(value: Double): String {
        val formatted = "%,.0f".format(value).replace(",", ".")
        return formatted
    }
}

enum class ParserType(val displayName: String) {
    GENERAL("Umum"),
    RESTAURANT("Resto"),
    RETAIL_THERMAL("Retail")
}

data class ParsedMenuItem(
    val name: String,
    val quantity: Int = 1,
    val price: Double,
    val subtotal: Double
)

interface ReceiptParser {
    fun parse(ocr: OcrResult): ParsedReceipt
}
