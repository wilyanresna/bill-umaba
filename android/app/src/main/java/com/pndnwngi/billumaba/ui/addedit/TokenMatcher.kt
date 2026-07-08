package com.pndnwngi.billumaba.ui.addedit

object TokenMatcher {
    /**
     * Checks if a text line matches the query using
     * token-based prefix matching, case-insensitive.
     *
     * The line is split into tokens by spaces.
     * Returns true if at least one token starts with the query.
     */
    fun matches(line: String, query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return true
        val queryWords = trimmed.lowercase().split(" ").filter { it.isNotEmpty() }
        val tokens = line.split(" ").map { it.lowercase() }
        return queryWords.all { queryWord ->
            tokens.any { token -> token.startsWith(queryWord) }
        }
    }

    /**
     * Filters a list of OCR lines.
     * For numeric fields, only lines containing at least one digit are included.
     */
    fun filter(
        lines: List<String>,
        query: String,
        numericOnly: Boolean = false
    ): List<String> {
        return lines
            .filter { line ->
                if (numericOnly) line.any { it.isDigit() } else true
            }
            .filter { line -> matches(line, query) }
    }
}
