package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.ocr.OcrResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetailThermalParser @Inject constructor(
    private val generalParser: GeneralReceiptParser
) : ReceiptParser {

    companion object {
        // Cash register markers (Indonesian)
        val CASH_KEYWORDS = listOf("tunai", "kembali", "kembalian", "bayar", "kartu", "debit", "credit", "cc", "dc")
        val PROMO_KEYWORDS = listOf("potongan", "disc", "hemat", "voucher", "promo", "bpjs")
        val TOTAL_KEYWORDS = listOf("total", "jumlah", "harga belanja", "total belanja", "grand total")

        // Format 1: "Nama  Qty x Harga = Subtotal" or "Nama  Qty x Harga"
        // Format 2: "Nama  Harga" (qty implicit 1)
        val ITEM_FORMAT_1 = Regex("""^(.+?)\s+(\d+)\s*[xX\*]\s*(\d[\d.,]*)\s*(?:=\s*(\d[\d.,]*))?$""")
        val ITEM_FORMAT_2 = Regex("""^(.+?)\s{2,}(\d[\d.,]*)$""")
    }

    override fun parse(ocr: OcrResult): ParsedReceipt {
        val texts = ocr.lines.map { it.text }

        val restaurantName = generalParser.extractRestaurantName(texts)
        val visitDate = generalParser.extractDate(texts)

        // Find the payment section start (first line with cash keyword)
        val paymentStartIndex = findPaymentSectionStart(texts)

        // Items: lines before payment section, skip promo/total lines
        val itemTexts = if (paymentStartIndex >= 0) {
            texts.subList(0, paymentStartIndex)
        } else {
            texts
        }
        val menuItems = extractRetailItems(itemTexts)

        // Summary section: lines from payment start to end
        val summaryTexts = if (paymentStartIndex >= 0 && paymentStartIndex < texts.size) {
            texts.subList(paymentStartIndex, texts.size)
        } else {
            // Look for total before the end
            emptyList()
        }

        val grandTotal = extractRetailGrandTotal(summaryTexts, itemTexts)
        val cashPaid = extractCashPaid(summaryTexts)
        val cashChange = extractCashChange(summaryTexts)

        return ParsedReceipt(
            detectedParserType = ParserType.RETAIL_THERMAL,
            restaurantName = restaurantName,
            menuItems = menuItems,
            grandTotal = grandTotal,
            tax = null,
            service = null,
            discount = null,
            visitDate = visitDate
        )
    }

    private fun findPaymentSectionStart(texts: List<String>): Int {
        for (i in texts.indices) {
            val lower = texts[i].lowercase().trim()
            if (CASH_KEYWORDS.any { kw -> lower.startsWith(kw) || lower.contains(" $kw") }) {
                return i
            }
        }
        return -1
    }

    private fun extractRetailItems(texts: List<String>): List<ParsedMenuItem> {
        val items = mutableListOf<ParsedMenuItem>()

        for (line in texts) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            val lower = trimmed.lowercase()
            // Skip promo, total, tax, service lines
            if (PROMO_KEYWORDS.any { lower.contains(it) }) continue
            if (TOTAL_KEYWORDS.any { lower.contains(it) }) continue
            if (lower.contains("pajak") || lower.contains("ppn")) continue
            if (lower.contains("service") || lower.contains("sub total")) continue
            if (lower.contains("tunai") || lower.contains("kembali")) continue
            if (lower.contains("kartu") || lower.contains("debit")) continue

            val parsed = parseRetailItemLine(trimmed)
            if (parsed != null) {
                items.add(parsed)
            }
        }

        return items
    }

    private fun parseRetailItemLine(line: String): ParsedMenuItem? {
        // Format 1: "Nasi Goreng  2 x 25.000 = 50.000"
        val fmt1 = ITEM_FORMAT_1.find(line)
        if (fmt1 != null) {
            val name = fmt1.groupValues[1].trim()
            val qty = fmt1.groupValues[2].toIntOrNull() ?: 1
            val price = generalParser.parsePrice(fmt1.groupValues[3]) ?: return null
            val subtotal = if (fmt1.groupValues[4].isNotBlank()) {
                generalParser.parsePrice(fmt1.groupValues[4]) ?: qty * price
            } else {
                qty * price
            }
            if (name.isBlank()) return null
            return ParsedMenuItem(name = name, quantity = qty, price = price, subtotal = subtotal)
        }

        // Format 2: "Nasi Goreng    25.000"
        val fmt2 = ITEM_FORMAT_2.find(line)
        if (fmt2 != null) {
            val name = fmt2.groupValues[1].trim()
            val price = generalParser.parsePrice(fmt2.groupValues[2]) ?: return null
            if (name.isBlank()) return null
            return ParsedMenuItem(name = name, quantity = 1, price = price, subtotal = price)
        }

        // Fallback: line with a single price at the end, no quantity
        val price = generalParser.extractPriceFromLine(line) ?: return null
        // Find name: text before the last number sequence
        val lastNumberStart = line.indexOfLast { it.isDigit() }
        if (lastNumberStart <= 0) return null
        // Walk back to find where the number starts (after spaces)
        var nameEnd = lastNumberStart
        while (nameEnd > 0 && line[nameEnd - 1].isWhitespace()) nameEnd--
        val name = line.substring(0, nameEnd).trim()
        if (name.isBlank()) return null
        return ParsedMenuItem(name = name, quantity = 1, price = price, subtotal = price)
    }

    private fun extractRetailGrandTotal(summaryTexts: List<String>, itemTexts: List<String>): Double? {
        // Look in summary section first
        for (line in summaryTexts) {
            val lower = line.lowercase().trim()
            if (TOTAL_KEYWORDS.any { lower.contains(it) }) {
                val price = generalParser.extractPriceFromLine(line)
                if (price != null) return price
            }
        }

        // Fallback: sum of items
        return itemTexts.sumOf { line ->
            parseRetailItemLine(line.trim())?.subtotal ?: 0.0
        }.takeIf { it > 0 }
    }

    private fun extractCashPaid(summaryTexts: List<String>): Double? {
        for (line in summaryTexts) {
            val lower = line.lowercase().trim()
            if (lower.startsWith("tunai") || lower.contains(" bayar ") || lower.startsWith("bayar")) {
                return generalParser.extractPriceFromLine(line)
            }
        }
        return null
    }

    private fun extractCashChange(summaryTexts: List<String>): Double? {
        for (line in summaryTexts) {
            val lower = line.lowercase().trim()
            if (lower.startsWith("kembali") || lower.startsWith("kembalian")) {
                return generalParser.extractPriceFromLine(line)
            }
        }
        return null
    }
}
