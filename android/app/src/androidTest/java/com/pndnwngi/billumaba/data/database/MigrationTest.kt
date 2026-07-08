package com.pndnwngi.billumaba.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    companion object {
        private const val TEST_DB_NAME = "migration-test-db"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate1To2_preservesExistingData() {
        // Create v1 database
        var db = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                INSERT INTO visits (restaurantName, restaurantAddress, restaurantRating, restaurantReview, visitDate, receiptPhotoPath, grandTotal)
                VALUES ('Warung Padang', 'Jl. Merdeka', 4.5, 'Enak', 1700000000000, '/path/photo.jpg', 50000.0)
                """.trimIndent()
            )
        }
        db.close()

        // Run migration
        db = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Verify existing visit still exists
        val cursor = db.query("SELECT restaurantName FROM visits")
        assertTrue(cursor.moveToFirst())
        assertEquals("Warung Padang", cursor.getString(0))

        // Verify receipt_patterns table exists and is empty
        val tables = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='receipt_patterns'"
        )
        assertTrue(tables.moveToFirst())
        assertEquals("receipt_patterns", tables.getString(0))

        val patternCount = db.query("SELECT COUNT(*) FROM receipt_patterns")
        assertTrue(patternCount.moveToFirst())
        assertEquals(0, patternCount.getInt(0))

        db.close()
    }
}
