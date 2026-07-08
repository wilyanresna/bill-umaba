package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.ocr.OcrLine
import com.pndnwngi.billumaba.data.ocr.OcrResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralReceiptParser @Inject constructor() : ReceiptParser {

    companion object {
        // Matches price: "25.000", "25,000", "Rp 25.000", "IDR 25000", "25.000-", etc.
        val PRICE_REGEX = Regex(
            """(?:Rp\.?\s*|IDR\s*)?(\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{1,2})?)\s*-?""",
            RegexOption.IGNORE_CASE
        )

        // Matches quantity prefix: "2x ", "2 x ", "2* ", "2X "
        val QTY_REGEX = Regex("""^(\d+)\s*[xX\*]\s*""")

        val TOTAL_KEYWORDS = listOf("grand total", "total", "jumlah", "tagihan", "amount due", "bayar")
        val TAX_KEYWORDS = listOf("pajak", "ppn", "pb1", "tax", "pajak restoran", "pajak hotel")
        val SERVICE_KEYWORDS = listOf("service", "service charge", "pelayanan", "svcharge", "svc")
        val DISCOUNT_KEYWORDS = listOf("diskon", "discount", "potongan", "voucher", "promo", "hemat")
        val SKIP_KEYWORDS = listOf(
            "subtotal", "sub total", "sub total", "total",
            "tunai", "kembali", "kembalian", "kartu", "debit", "credit",
            "kasir", "kasir:", "shift", "no.", "no:", "trx", "struk"
        )

        val ADDRESS_KEYWORDS = listOf("jl", "jalan", "no.", "telp", "phone", "telepon", "fax", "kode pos", "rt/rw", "kel", "kec")

        // Date patterns: dd/MM/yyyy, dd-MM-yy, dd MMM yyyy, yyyy-MM-dd
        val DATE_REGEXES = listOf(
            Regex("""(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{2,4})"""),
            Regex("""(\d{1,2})\s+(Jan|Feb|Mar|Apr|Mei|Jun|Jul|Agu|Sep|Okt|Nov|Des)\w*\s+(\d{2,4})""", RegexOption.IGNORE_CASE),
            Regex("""(\d{4})[/\-.](\d{1,2})[/\-.](\d{1,2})""")
        )
    }

    override fun parse(ocr: OcrResult): ParsedReceipt {
        val lines = ocr.lines
        val texts = lines.map { it.text }

        val restaurantName = extractRestaurantName(texts)
        val menuItems = extractMenuItems(texts)
        val grandTotal = extractGrandTotal(texts)
        val tax = extractByKeywords(texts, TAX_KEYWORDS)
        val service = extractByKeywords(texts, SERVICE_KEYWORDS)
        val discount = extractByKeywords(texts, DISCOUNT_KEYWORDS)
        val visitDate = extractDate(texts)

        return ParsedReceipt(
            detectedParserType = ParserType.GENERAL,
            restaurantName = restaurantName,
            menuItems = menuItems,
            grandTotal = grandTotal,
            tax = tax,
            service = service,
            discount = discount,
            visitDate = visitDate
        )
    }

    fun extractRestaurantName(texts: List<String>): String? {
        if (texts.isEmpty()) return null

        // Take first 1-2 lines, filter out address keywords
        val candidates = texts.take(2).filter { line ->
            val lower = line.lowercase().trim()
            lower.isNotBlank() &&
                    ADDRESS_KEYWORDS.none { kw -> lower.contains(kw) } &&
                    !lower.contains("struck") &&
                    !lower.contains("struk") &&
                    !lower.contains("bon") &&
                    !lower.contains("nota")
        }
        return candidates.joinToString(" ").trim().ifBlank { null }
    }

    fun extractMenuItems(texts: List<String>): List<ParsedMenuItem> {
        val items = mutableListOf<ParsedMenuItem>()

        for (line in texts) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            // Skip lines with total/tax/service/discount keywords
            val lower = trimmed.lowercase()
            if (TOTAL_KEYWORDS.any { lower.contains(it) }) continue
            if (TAX_KEYWORDS.any { lower.contains(it) }) continue
            if (SERVICE_KEYWORDS.any { lower.contains(it) }) continue
            if (DISCOUNT_KEYWORDS.any { lower.contains(it) }) continue
            if (SKIP_KEYWORDS.any { lower.contains(it) }) continue

            val parsed = parseMenuLine(trimmed)
            if (parsed != null) {
                items.add(parsed)
            }
        }

        return items
    }

    fun parseMenuLine(line: String): ParsedMenuItem? {
        // Try to find a price in the line
        val prices = PRICE_REGEX.findAll(line).toList()
        if (prices.isEmpty()) return null

        // The last price in the line is typically the subtotal or unit price
        val lastPrice = prices.last()
        val priceValue = parsePrice(lastPrice.groupValues[1]) ?: return null

        // Check for quantity prefix
        val qtyMatch = QTY_REGEX.find(line)
        val quantity = qtyMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1

        // Extract name: text before the price, removing quantity prefix
        val nameStart = if (qtyMatch != null) qtyMatch.range.last + 1 else 0
        val nameEnd = lastPrice.range.first
        val name = line.substring(nameStart, nameEnd)
            .replace(Regex("""\s+"""), " ")
            .trim()
            .trimEnd('-', '.', ',')

        if (name.isBlank()) return null

        return ParsedMenuItem(
            name = name,
            quantity = quantity,
            price = priceValue,
            subtotal = quantity * priceValue
        )
    }

    fun extractGrandTotal(texts: List<String>): Double? {
        var bestTotal: Double? = null
        var bestPriority = -1

        for (line in texts) {
            val lower = line.lowercase().trim()
            for ((priority, keyword) in TOTAL_KEYWORDS.withIndex()) {
                if (lower.contains(keyword)) {
                    val priceOnLine = extractPriceFromLine(line)
                    if (priceOnLine != null) {
                        if (bestTotal == null || priceOnLine > bestTotal || priority > bestPriority) {
                            bestTotal = priceOnLine
                            bestPriority = priority
                        }
                    }
                }
            }
        }

        return bestTotal
    }

    fun extractByKeywords(texts: List<String>, keywords: List<String>): Double? {
        for (line in texts) {
            val lower = line.lowercase().trim()
            if (keywords.any { lower.contains(it) }) {
                val price = extractPriceFromLine(line)
                if (price != null) return price
            }
        }
        return null
    }

    fun extractPriceFromLine(line: String): Double? {
        val prices = PRICE_REGEX.findAll(line).toList()
        if (prices.isEmpty()) return null
        // Return the last price found on the line
        return parsePrice(prices.last().groupValues[1])
    }

    fun extractDate(texts: List<String>): Long? {
        for (line in texts) {
            for (regex in DATE_REGEXES) {
                val match = regex.find(line) ?: continue
                val dateMs = parseDateFromMatch(match) ?: continue
                return dateMs
            }
        }
        return null
    }

    private fun parseDateFromMatch(match: MatchResult): Long? {
        try {
            val groups = match.groupValues
            val cal = java.util.Calendar.getInstance()

            when {
                // yyyy-MM-dd or yyyy/MM/yyyy
                groups[1].length == 4 -> {
                    cal.set(java.util.Calendar.YEAR, groups[1].toInt())
                    cal.set(java.util.Calendar.MONTH, groups[2].toInt() - 1)
                    cal.set(java.util.Calendar.DAY_OF_MONTH, groups[3].toInt())
                }
                // dd MMM yyyy
                groups[2].length > 2 -> {
                    val monthNames = listOf(
                        "jan", "feb", "mar", "apr", "mei", "jun",
                        "jul", "agu", "sep", "okt", "nov", "des"
                    )
                    val month = monthNames.indexOf(groups[2].lowercase().take(3))
                    if (month < 0) return null
                    cal.set(java.util.Calendar.DAY_OF_MONTH, groups[1].toInt())
                    cal.set(java.util.Calendar.MONTH, month)
                    cal.set(java.util.Calendar.YEAR, groups[3].toInt())
                }
                // dd/MM/yyyy or dd-MM-yy
                else -> {
                    cal.set(java.util.Calendar.DAY_OF_MONTH, groups[1].toInt())
                    cal.set(java.util.Calendar.MONTH, groups[2].toInt() - 1)
                    var year = groups[3].toInt()
                    if (year < 100) year += 2000
                    cal.set(java.util.Calendar.YEAR, year)
                }
            }
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        } catch (e: Exception) {
            return null
        }
    }

    fun parsePrice(value: String): Double? {
        val cleaned = value.replace(" ", "").trim()
        if (cleaned.isBlank()) return null

        // Handle "25.000" (Indonesian thousand separator with dot)
        // Handle "25,000" (could be decimal or thousand sep)
        // Handle "25000" (plain number)
        // Handle "25.000,50" (mixed)

        return try {
            val normalized = when {
                cleaned.contains(",") && cleaned.contains(".") -> {
                    // Could be "25.000,50" or "25,000.50"
                    val lastComma = cleaned.lastIndexOf(',')
                    val lastDot = cleaned.lastIndexOf('.')
                    if (lastComma > lastDot) {
                        // "25.000,50" format
                        cleaned.replace(".", "").replace(",", ".")
                    } else {
                        // "25,000.50" format
                        cleaned.replace(",", "")
                    }
                }
                cleaned.contains(",") -> {
                    // "25,000" or "25,5"
                    val commaParts = cleaned.split(",")
                    if (commaParts.last().length <= 2) {
                        // Decimal: "25,50"
                        cleaned.replace(",", ".")
                    } else {
                        // Thousand sep: "25,000"
                        cleaned.replace(",", "")
                    }
                }
                cleaned.contains(".") -> {
                    // "25.000" or "25.5"
                    val dotParts = cleaned.split(".")
                    if (dotParts.last().length <= 2 && dotParts.size == 2) {
                        // Could be decimal "25.50" or thousand "25.000"
                        // If 3 digits after dot → thousand sep
                        if (dotParts.last().length == 3 && dotParts[0].toIntOrNull() != null) {
                            cleaned.replace(".", "")
                        } else {
                            cleaned
                        }
                    } else if (dotParts.last().length == 3 && dotParts.size > 2) {
                        // "1.250.000" → thousand sep
                        cleaned.replace(".", "")
                    } else {
                        cleaned
                    }
                }
                else -> cleaned
            }
            normalized.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
