package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.ocr.OcrLine
import com.pndnwngi.billumaba.data.ocr.OcrResult
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReceiptParserFactoryTest {

    private lateinit var factory: ReceiptParserFactory

    @Before
    fun setUp() {
        factory = ReceiptParserFactory(
            generalParser = GeneralReceiptParser(),
            restaurantParser = RestaurantReceiptParser(GeneralReceiptParser()),
            retailParser = RetailThermalParser(GeneralReceiptParser())
        )
    }

    private fun ocrFromLines(vararg lines: String): OcrResult {
        return OcrResult(
            lines = lines.map { OcrLine(text = it, boundingBox = null, confidence = 0.95f) }
        )
    }

    @Test
    fun `auto detect general when no markers`() {
        val ocr = ocrFromLines(
            "Kopi Kenangan",
            "Es Kopi  28.000",
            "Total    28.000"
        )
        assertEquals(ParserType.GENERAL, factory.autoDetectType(ocr))
    }

    @Test
    fun `auto detect restaurant when subtotal and pajak`() {
        val ocr = ocrFromLines(
            "Warung Padang",
            "Nasi Goreng  25.000",
            "Subtotal     25.000",
            "Pajak 10%     2.500",
            "Grand Total  27.500"
        )
        assertEquals(ParserType.RESTAURANT, factory.autoDetectType(ocr))
    }

    @Test
    fun `auto detect retail when tunai keyword`() {
        val ocr = ocrFromLines(
            "Indomaret",
            "Indomie      3.500",
            "Total         3.500",
            "Tunai        5.000",
            "Kembali      1.500"
        )
        assertEquals(ParserType.RETAIL_THERMAL, factory.autoDetectType(ocr))
    }

    @Test
    fun `auto detect retail when kembali keyword`() {
        val ocr = ocrFromLines(
            "Alfamart",
            "Item 1    5.000",
            "Total      5.000",
            "Bayar     10.000",
            "Kembali    5.000"
        )
        assertEquals(ParserType.RETAIL_THERMAL, factory.autoDetectType(ocr))
    }

    @Test
    fun `override type takes priority`() {
        val ocr = ocrFromLines(
            "Indomaret",
            "Item 1    5.000",
            "Tunai     10.000"
        )
        // Even though auto-detect says RETAIL, override to GENERAL
        val result = factory.parse(ocr, overrideType = ParserType.GENERAL)
        assertEquals(ParserType.GENERAL, result.detectedParserType)
    }

    @Test
    fun `parse general receipt`() {
        val ocr = ocrFromLines(
            "Kopi Kenangan",
            "Es Kopi  28.000",
            "Matcha   32.000",
            "Total    60.000"
        )
        val result = factory.parse(ocr)
        assertEquals(ParserType.GENERAL, result.detectedParserType)
        assertEquals(2, result.menuItems.size)
    }

    @Test
    fun `parse restaurant receipt`() {
        val ocr = ocrFromLines(
            "Warung Padang",
            "Nasi Goreng  25.000",
            "Subtotal     25.000",
            "Pajak 10%     2.500",
            "Service 5%    1.250",
            "Grand Total  28.750"
        )
        val result = factory.parse(ocr)
        assertEquals(ParserType.RESTAURANT, result.detectedParserType)
        assertEquals(28750.0, result.grandTotal!!, 0.01)
        assertEquals(2500.0, result.tax!!, 0.01)
        assertEquals(1250.0, result.service!!, 0.01)
    }

    @Test
    fun `parse retail receipt`() {
        val ocr = ocrFromLines(
            "Indomaret",
            "Indomie    3.500",
            "Aqua       4.000",
            "Total      7.500",
            "Tunai     10.000",
            "Kembali    2.500"
        )
        val result = factory.parse(ocr)
        assertEquals(ParserType.RETAIL_THERMAL, result.detectedParserType)
        assertEquals(2, result.menuItems.size)
    }
}
