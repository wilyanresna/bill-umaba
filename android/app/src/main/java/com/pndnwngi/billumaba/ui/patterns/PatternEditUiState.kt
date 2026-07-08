package com.pndnwngi.billumaba.ui.patterns

import com.pndnwngi.billumaba.data.parser.ParserType

data class PatternEditUiState(
    val id: Long? = null,
    val restaurantName: String = "",
    val displayName: String = "",
    val parserType: ParserType = ParserType.GENERAL,
    val restaurantNameStrategy: NameStrategy = NameStrategy.AUTO_TOP,
    val menuLineTemplate: String = "{qty}x {name} {price}",
    val separator: Separator = Separator.X,
    val totalLineStrategy: TotalStrategy = TotalStrategy.BIGGEST_TOTAL_KEYWORD,
    val totalLineRegex: String = "",
    val taxEnabled: Boolean = false,
    val taxLineRegex: String = "",
    val serviceEnabled: Boolean = false,
    val serviceLineRegex: String = "",
    val discountEnabled: Boolean = false,
    val discountLineRegex: String = "",
    val dateEnabled: Boolean = false,
    val dateRegex: String = "",
    val headerLineCount: Int = 2,
    val skipKeywords: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showAdvanced: Boolean = false,
    val testPhotoUri: String? = null,
    val testResult: com.pndnwngi.billumaba.data.parser.ParsedReceipt? = null,
    val isRunningTest: Boolean = false
)

enum class NameStrategy(val displayName: String) {
    FIRST_LINE("Baris pertama"),
    FIRST_TWO_LINES("Dua baris pertama"),
    AUTO_TOP("Auto detect (line teratas non-alamat)")
}

enum class TotalStrategy(val displayName: String) {
    BIGGEST_TOTAL_KEYWORD("Baris dengan 'Total' terbesar"),
    LAST_LINE("Baris terakhir"),
    CUSTOM_REGEX("Custom regex")
}

enum class Separator(val displayName: String) {
    X("x"),
    SPACE("spasi"),
    DASH("-"),
    CUSTOM("Custom")
}
