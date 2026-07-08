package com.pndnwngi.billumaba.data.parser

import com.pndnwngi.billumaba.data.ocr.OcrLine
import com.pndnwngi.billumaba.data.ocr.OcrResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RestaurantReceiptParserTest {

    private lateinit var parser: RestaurantReceiptParser
    private lateinit var generalParser: GeneralReceiptParser

    @Before
    fun setUp() {
        generalParser = GeneralReceiptParser()
        parser = RestaurantReceiptParser(generalParser)
    }

    private fun ocrFromLines(vararg lines: String): OcrResult {
        return OcrResult(
            lines = lines.map { OcrLine(text = it, boundingBox = null, confidence = 0.95f) }
        )
    }

    @Test
    fun `parse restaurant receipt with tax and service`() {
        val ocr = ocrFromLines(
            "Warung Padang Sederhana",
            "Jl. Gatot Subroto No. 45",
            "",
            "Nasi Rendang      35.000",
            "Ayam Bakar        40.000",
            "Es Teh Manis       8.000",
            "Padang Campur     45.000",
            "",
            "Subtotal         128.000",
            "Pajak Restoran 10% 12.800",
            "Service Charge 5%   6.400",
            "Diskon Membership -10.000",
            "",
            "Grand Total      137.200",
            "",
            "Tunai           150.000",
            "Kembali          12.800"
        )

        val result = parser.parse(ocr)

        assertEquals(ParserType.RESTAURANT, result.detectedParserType)
        assertEquals("Warung Padang Sederhana", result.restaurantName)
        assertEquals(4, result.menuItems.size)
        assertEquals(137200.0, result.grandTotal!!, 0.01)
        assertNotNull(result.tax)
        assertEquals(12800.0, result.tax!!, 0.01)
        assertNotNull(result.service)
        assertEquals(6400.0, result.service!!, 0.01)
        assertNotNull(result.discount)
        assertEquals(10000.0, result.discount!!, 0.01)
    }

    @Test
    fun `items only include lines before subtotal`() {
        val ocr = ocrFromLines(
            "Nasi Goreng  25.000",
            "Es Teh        8.000",
            "Subtotal     33.000",
            "Pajak 10%     3.300",
            "Grand Total  36.300"
        )

        val result = parser.parse(ocr)

        assertEquals(2, result.menuItems.size)
        assertEquals("Nasi Goreng", result.menuItems[0].name)
        assertEquals("Es Teh", result.menuItems[1].name)
    }

    @Test
    fun `grand total prioritizes grand total over subtotal`() {
        val ocr = ocrFromLines(
            "Item 1  10.000",
            "Subtotal 10.000",
            "Pajak 10% 1.000",
            "Grand Total 11.000"
        )

        val result = parser.parse(ocr)
        assertEquals(11000.0, result.grandTotal!!, 0.01)
    }

    @Test
    fun `falls back to subtotal when no grand total`() {
        val ocr = ocrFromLines(
            "Item 1  10.000",
            "Subtotal 10.000",
            "Pajak 10% 1.000"
        )

        val result = parser.parse(ocr)
        assertEquals(10000.0, result.grandTotal!!, 0.01)
    }

    @Test
    fun `restaurant name extracted from first lines`() {
        val ocr = ocrFromLines(
            "Bakso Urat Mas",
            "Jl. Merdeka No. 1",
            "Bakso Urat  25.000",
            "Subtotal    25.000",
            "Grand Total 27.500"
        )

        val result = parser.parse(ocr)
        assertEquals("Bakso Urat Mas", result.restaurantName)
    }
}
