package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.ocr.OcrLine
import com.pndnwngi.billumaba.data.ocr.OcrResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeneralReceiptParserTest {

    private lateinit var parser: GeneralReceiptParser

    @Before
    fun setUp() {
        parser = GeneralReceiptParser()
    }

    private fun ocrFromLines(vararg lines: String): OcrResult {
        return OcrResult(
            lines = lines.map { OcrLine(text = it, boundingBox = null, confidence = 0.95f) }
        )
    }

    @Test
    fun `parse simple cafe receipt`() {
        val ocr = ocrFromLines(
            "Kopi Kenangan",
            "Jl. Sudirman No. 123",
            "",
            "Es Kopi       28.000",
            "Matcha        32.000",
            "Croissant     25.000",
            "",
            "Total         85.000"
        )

        val result = parser.parse(ocr)

        assertEquals(ParserType.GENERAL, result.detectedParserType)
        assertEquals("Kopi Kenangan", result.restaurantName)
        assertEquals(3, result.menuItems.size)
        assertEquals(85000.0, result.grandTotal!!, 0.01)
        assertNull(result.tax)
        assertNull(result.service)
    }

    @Test
    fun `extractRestaurantName skips address lines`() {
        val texts = listOf(
            "Kopi Kenangan",
            "Jl. Sudirman No. 123",
            "Es Kopi 28.000"
        )
        val name = parser.extractRestaurantName(texts)
        assertEquals("Kopi Kenangan", name)
    }

    @Test
    fun `extractRestaurantName returns null for empty input`() {
        assertNull(parser.extractRestaurantName(emptyList()))
    }

    @Test
    fun `extractMenuItems skips total lines`() {
        val texts = listOf(
            "Es Kopi       28.000",
            "Matcha        32.000",
            "Total         60.000"
        )
        val items = parser.extractMenuItems(texts)
        assertEquals(2, items.size)
        assertEquals("Es Kopi", items[0].name)
        assertEquals(28000.0, items[0].price, 0.01)
    }

    @Test
    fun `extractGrandTotal finds largest total keyword`() {
        val texts = listOf(
            "Subtotal      60.000",
            "Grand Total   65.000"
        )
        val total = parser.extractGrandTotal(texts)
        assertEquals(65000.0, total!!, 0.01)
    }

    @Test
    fun `parsePrice handles Indonesian format`() {
        assertEquals(25000.0, parser.parsePrice("25.000")!!, 0.01)
        assertEquals(1250000.0, parser.parsePrice("1.250.000")!!, 0.01)
        assertEquals(25000.0, parser.parsePrice("25000")!!, 0.01)
        assertEquals(25500.0, parser.parsePrice("25,500")!!, 0.01)
    }

    @Test
    fun `extractDate finds dd MM yyyy`() {
        val texts = listOf("08/07/2026 10:30", "Kopi Kenangan")
        val date = parser.extractDate(texts)
        assertTrue(date != null && date > 0)
    }

    @Test
    fun `menuItems have correct quantity and subtotal`() {
        val texts = listOf(
            "2x Nasi Goreng  25.000",
            "1x Es Teh        8.000"
        )
        val items = parser.extractMenuItems(texts)
        assertEquals(2, items.size)
        assertEquals(2, items[0].quantity)
        assertEquals(50000.0, items[0].subtotal, 0.01)
        assertEquals(1, items[1].quantity)
        assertEquals(8000.0, items[1].subtotal, 0.01)
    }
}
