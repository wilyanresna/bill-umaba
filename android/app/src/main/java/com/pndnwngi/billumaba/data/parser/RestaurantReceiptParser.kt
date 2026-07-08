package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.ocr.OcrResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantReceiptParser @Inject constructor(
    private val generalParser: GeneralReceiptParser
) : ReceiptParser {

    companion object {
        val SUBTOTAL_KEYWORDS = listOf("subtotal", "sub total", "sub total")
        val DISCOUNT_SECTION_KEYWORDS = listOf("diskon", "discount", "potongan", "voucher", "promo", "hemat")
    }

    override fun parse(ocr: OcrResult): ParsedReceipt {
        val texts = ocr.lines.map { it.text }

        val restaurantName = generalParser.extractRestaurantName(texts)
        val visitDate = generalParser.extractDate(texts)

        // Find the subtotal line index to use as boundary
        val subtotalIndex = findSubtotalLineIndex(texts)

        // Menu items: only lines BEFORE the subtotal line
        val menuTexts = if (subtotalIndex >= 0) {
            texts.subList(0, subtotalIndex)
        } else {
            texts
        }
        val menuItems = generalParser.extractMenuItems(menuTexts)

        // Summary section: lines AFTER subtotal
        val summaryTexts = if (subtotalIndex >= 0 && subtotalIndex + 1 < texts.size) {
            texts.subList(subtotalIndex + 1, texts.size)
        } else {
            emptyList()
        }

        val grandTotal = extractRestaurantGrandTotal(summaryTexts)
        val subtotalValue = if (subtotalIndex >= 0) {
            generalParser.extractPriceFromLine(texts[subtotalIndex])
        } else {
            null
        }

        val tax = generalParser.extractByKeywords(summaryTexts, listOf("pajak", "ppn", "pb1", "tax", "pajak restoran"))
        val service = generalParser.extractByKeywords(summaryTexts, listOf("service", "service charge", "pelayanan", "svcharge", "svc"))
        val discount = extractDiscount(summaryTexts)

        return ParsedReceipt(
            detectedParserType = ParserType.RESTAURANT,
            restaurantName = restaurantName,
            menuItems = menuItems,
            grandTotal = grandTotal ?: subtotalValue,
            tax = tax,
            service = service,
            discount = discount,
            visitDate = visitDate
        )
    }

    private fun findSubtotalLineIndex(texts: List<String>): Int {
        // Find the FIRST line containing subtotal keyword
        for (i in texts.indices) {
            val lower = texts[i].lowercase().trim()
            if (SUBTOTAL_KEYWORDS.any { lower.contains(it) }) {
                return i
            }
        }
        return -1
    }

    private fun extractRestaurantGrandTotal(summaryTexts: List<String>): Double? {
        var bestTotal: Double? = null

        for (line in summaryTexts) {
            val lower = line.lowercase().trim()
            // Prioritize "grand total" > "total" > "jumlah"
            if (lower.contains("grand total") || lower.contains("total bayar") || lower.contains("jumlah bayar")) {
                val price = generalParser.extractPriceFromLine(line)
                if (price != null) return price
            }
            if (lower.contains("total") && !lower.contains("subtotal") && !lower.contains("sub total")) {
                val price = generalParser.extractPriceFromLine(line)
                if (price != null && (bestTotal == null || price > bestTotal)) {
                    bestTotal = price
                }
            }
        }
        return bestTotal
    }

    private fun extractDiscount(summaryTexts: List<String>): Double? {
        for (line in summaryTexts) {
            val lower = line.lowercase().trim()
            if (DISCOUNT_SECTION_KEYWORDS.any { lower.contains(it) }) {
                val price = generalParser.extractPriceFromLine(line)
                if (price != null) return price
            }
        }
        return null
    }
}
