package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.database.entities.ReceiptPatternEntity
import com.pndnwngi.billumaba.data.ocr.OcrResult

class PatternReceiptParser(private val pattern: ReceiptPatternEntity) : ReceiptParser {

    private val menuRegex: Regex? by lazy {
        if (pattern.menuLineTemplate.isNotBlank()) {
            TemplateToRegex.convert(pattern.menuLineTemplate)
        } else null
    }

    private val taxRegex: Regex? by lazy {
        pattern.taxLineRegex?.takeIf { it.isNotBlank() }?.let {
            try { Regex(it, RegexOption.IGNORE_CASE) } catch (_: Exception) { null }
        }
    }

    private val serviceRegex: Regex? by lazy {
        pattern.serviceLineRegex?.takeIf { it.isNotBlank() }?.let {
            try { Regex(it, RegexOption.IGNORE_CASE) } catch (_: Exception) { null }
        }
    }

    private val discountRegex: Regex? by lazy {
        pattern.discountLineRegex?.takeIf { it.isNotBlank() }?.let {
            try { Regex(it, RegexOption.IGNORE_CASE) } catch (_: Exception) { null }
        }
    }

    private val dateRegex: Regex? by lazy {
        pattern.dateRegex?.takeIf { it.isNotBlank() }?.let {
            try { Regex(it, RegexOption.IGNORE_CASE) } catch (_: Exception) { null }
        }
    }

    private val totalRegex: Regex? by lazy {
        pattern.totalLineRegex?.takeIf { it.isNotBlank() }?.let {
            try { Regex(it, RegexOption.IGNORE_CASE) } catch (_: Exception) { null }
        }
    }

    private val skipKeywordsList: List<String> by lazy {
        pattern.skipKeywords
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
    }

    override fun parse(ocr: OcrResult): ParsedReceipt {
        val texts = ocr.lines.map { it.text }
        val filteredTexts = filterSkipKeywords(texts)

        val restaurantName = extractRestaurantName(filteredTexts)
        val menuItems = extractMenuItems(filteredTexts)
        val grandTotal = extractGrandTotal(filteredTexts)
        val tax = taxRegex?.let { extractByRegex(filteredTexts, it) }
        val service = serviceRegex?.let { extractByRegex(filteredTexts, it) }
        val discount = discountRegex?.let { extractByRegex(filteredTexts, it) }
        val visitDate = dateRegex?.let { extractDateByRegex(filteredTexts, it) }

        return ParsedReceipt(
            detectedParserType = ParserType.valueOf(pattern.parserType),
            restaurantName = restaurantName,
            menuItems = menuItems,
            grandTotal = grandTotal,
            tax = tax,
            service = service,
            discount = discount,
            visitDate = visitDate
        )
    }

    private fun filterSkipKeywords(texts: List<String>): List<String> {
        if (skipKeywordsList.isEmpty()) return texts
        return texts.filter { line ->
            val lower = line.lowercase()
            skipKeywordsList.none { keyword -> lower.contains(keyword) }
        }
    }

    private fun extractRestaurantName(texts: List<String>): String? {
        return when (pattern.restaurantNameStrategy) {
            "FIRST_LINE" -> texts.firstOrNull()?.trim()
            "FIRST_TWO_LINES" -> texts.take(2).joinToString(" ") { it.trim() }
            else -> {
                // AUTO_TOP: 1-2 top lines, filter address keywords
                val addressKeywords = listOf("jl.", "jalan", "no.", "komplek", "kota", "kab")
                val topLines = texts.take(pattern.headerLineCount).filter { line ->
                    val lower = line.lowercase()
                    addressKeywords.none { lower.contains(it) }
                }
                topLines.joinToString(" ").trim().ifBlank { texts.firstOrNull() }
            }
        }
    }

    private fun extractMenuItems(texts: List<String>): List<ParsedMenuItem> {
        val regex = menuRegex ?: return emptyList()
        val items = mutableListOf<ParsedMenuItem>()

        for (line in texts) {
            val match = regex.find(line) ?: continue
            val qty = match.groups["qty"]?.value?.toIntOrNull() ?: 1
            val name = match.groups["name"]?.value?.trim() ?: continue
            val price = parsePrice(match.groups["price"]?.value ?: continue)
            val subtotal = match.groups["subtotal"]?.value?.let { parsePrice(it) } ?: (qty * price)

            if (name.isNotBlank() && price > 0) {
                items.add(
                    ParsedMenuItem(
                        name = name,
                        quantity = qty,
                        price = price,
                        subtotal = subtotal
                    )
                )
            }
        }
        return items
    }

    private fun extractGrandTotal(texts: List<String>): Double? {
        val totalKeywords = listOf("grand total", "total", "jumlah")

        when (pattern.totalLineStrategy) {
            "LAST_LINE" -> {
                for (line in texts.reversed()) {
                    val price = parsePriceFromLine(line)
                    if (price != null && price > 0) return price
                }
                return null
            }
            "CUSTOM_REGEX" -> {
                return totalRegex?.let { extractByRegex(texts, it) }
            }
            else -> {
                // BIGGEST_TOTAL_KEYWORD
                var bestTotal: Double? = null
                for (line in texts) {
                    val lower = line.lowercase()
                    if (totalKeywords.any { lower.contains(it) }) {
                        val price = parsePriceFromLine(line)
                        if (price != null && (bestTotal == null || price > bestTotal)) {
                            bestTotal = price
                        }
                    }
                }
                return bestTotal
            }
        }
    }

    private fun extractByRegex(texts: List<String>, regex: Regex): Double? {
        for (line in texts) {
            val match = regex.find(line) ?: continue
            val priceStr = match.value.replace(Regex("[^\\d.,]"), "")
            val price = parsePrice(priceStr)
            if (price > 0) return price
        }
        return null
    }

    private fun extractDateByRegex(texts: List<String>, regex: Regex): Long? {
        for (line in texts) {
            val match = regex.find(line) ?: continue
            try {
                val dateStr = match.value
                val formats = listOf(
                    "dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "dd-MM-yy",
                    "yyyy-MM-dd", "dd MMM yyyy"
                )
                for (format in formats) {
                    try {
                        val sdf = java.text.SimpleDateFormat(format, java.util.Locale.US)
                        val date = sdf.parse(dateStr)
                        if (date != null) return date.time
                    } catch (_: Exception) {
                        // try next format
                    }
                }
            } catch (_: Exception) {
                // skip
            }
        }
        return null
    }

    private fun parsePriceFromLine(line: String): Double? {
        val priceRegex = Regex("(\\d[\\d.,]*)")
        val matches = priceRegex.findAll(line).toList()
        return matches.lastOrNull()?.let { parsePrice(it.value) }
    }

    private fun parsePrice(value: String): Double {
        val cleaned = value.replace("Rp", "", ignoreCase = true)
            .replace("IDR", "", ignoreCase = true)
            .trim()
        if (cleaned.isBlank()) return 0.0
        val normalized = cleaned.replace(".", "").replace(",", ".")
        return normalized.toDoubleOrNull() ?: 0.0
    }
}
