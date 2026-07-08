package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.ocr.OcrLine
import com.pndnwngi.billumaba.data.ocr.OcrResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RetailThermalParserTest {

    private lateinit var parser: RetailThermalParser
    private lateinit var generalParser: GeneralReceiptParser

    @Before
    fun setUp() {
        generalParser = GeneralReceiptParser()
        parser = RetailThermalParser(generalParser)
    }

    private fun ocrFromLines(vararg lines: String): OcrResult {
        return OcrResult(
            lines = lines.map { OcrLine(text = it, boundingBox = null, confidence = 0.95f) }
        )
    }

    @Test
    fun `parse indomaret receipt with qty x price format`() {
        val ocr = ocrFromLines(
            "Indomaret",
            "Jl. Thamrin No. 10",
            "",
            "Indomie Goreng    2 x 3.500 = 7.000",
            "Aqua 600ml            4.000",
            "Rokok Surya 12       18.000",
            "",
            "Total Belanja        29.000",
            "Potongan Member      -2.000",
            "",
            "Tunai               30.000",
            "Kembali               1.000"
        )

        val result = parser.parse(ocr)

        assertEquals(ParserType.RETAIL_THERMAL, result.detectedParserType)
        assertEquals("Indomaret", result.restaurantName)
        assertEquals(3, result.menuItems.size)

        // Indomie Goreng: qty=2, price=3500, subtotal=7000
        val indomie = result.menuItems.find { it.name.contains("Indomie") }
        assertNotNull(indomie!!)
        assertEquals(2, indomie.quantity)
        assertEquals(3500.0, indomie.price, 0.01)
        assertEquals(7000.0, indomie.subtotal, 0.01)

        // Aqua: qty=1, price=4000
        val aqua = result.menuItems.find { it.name.contains("Aqua") }
        assertNotNull(aqua!!)
        assertEquals(1, aqua.quantity)
        assertEquals(4000.0, aqua.price, 0.01)
    }

    @Test
    fun `total is found before tunai section`() {
        val ocr = ocrFromLines(
            "Indomaret",
            "Item 1    5.000",
            "Item 2   10.000",
            "Total Belanja  15.000",
            "Tunai         20.000",
            "Kembali        5.000"
        )

        val result = parser.parse(ocr)
        assertEquals(15000.0, result.grandTotal!!, 0.01)
    }

    @Test
    fun `promo lines are skipped`() {
        val ocr = ocrFromLines(
            "Indomaret",
            "Indomie       3.500",
            "Hemat Disc    -1.000",
            "Total          2.500",
            "Tunai          5.000",
            "Kembali        2.500"
        )

        val result = parser.parse(ocr)
        // Only Indomie should be in items, not "Hemat Disc"
        assertEquals(1, result.menuItems.size)
        assertEquals("Indomie", result.menuItems[0].name)
    }

    @Test
    fun `format 2 - name and price only`() {
        val ocr = ocrFromLines(
            "Alfamart",
            "Sabun Lifebuoy    8.500",
            "Shampo Pantene   15.000",
            "Total             23.500",
            "Tunai             25.000",
            "Kembali            1.500"
        )

        val result = parser.parse(ocr)
        assertEquals(2, result.menuItems.size)
        assertEquals(8500.0, result.menuItems[0].price, 0.01)
        assertEquals(15000.0, result.menuItems[1].price, 0.01)
        assertEquals(23500.0, result.grandTotal!!, 0.01)
    }

    @Test
    fun `tax and service are null for retail`() {
        val ocr = ocrFromLines(
            "Indomaret",
            "Item 1    5.000",
            "Total      5.000",
            "Tunai     10.000",
            "Kembali    5.000"
        )

        val result = parser.parse(ocr)
        assertNull(result.tax)
        assertNull(result.service)
    }
}
