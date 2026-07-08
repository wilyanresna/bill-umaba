package com.pndnwngi.billumaba.data.parser

object TemplateToRegex {

    private val TEMPLATE_TOKENS = mapOf(
        "{qty}" to "(?<qty>\\d+)",
        "{name}" to "(?<name>.+?)",
        "{price}" to "(?<price>[\\d.,]+)",
        "{subtotal}" to "(?<subtotal>[\\d.,]+)"
    )

    fun convert(template: String): Regex {
        var pattern = Regex.escape(template)
        TEMPLATE_TOKENS.forEach { (token, regex) ->
            val escapedToken = Regex.escape(token)
            pattern = pattern.replace(escapedToken, regex)
        }
        return Regex(pattern, RegexOption.IGNORE_CASE)
    }
}
