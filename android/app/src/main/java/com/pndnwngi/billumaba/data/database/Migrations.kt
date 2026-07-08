package com.pndnwngi.billumaba.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS receipt_patterns (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                restaurantName TEXT NOT NULL,
                displayName TEXT NOT NULL,
                menuLineTemplate TEXT NOT NULL,
                totalLineStrategy TEXT NOT NULL,
                totalLineRegex TEXT,
                taxLineRegex TEXT,
                serviceLineRegex TEXT,
                discountLineRegex TEXT,
                dateRegex TEXT,
                restaurantNameStrategy TEXT NOT NULL,
                headerLineCount INTEGER NOT NULL DEFAULT 2,
                skipKeywords TEXT NOT NULL DEFAULT '',
                parserType TEXT NOT NULL DEFAULT 'GENERAL',
                createdAt INTEGER NOT NULL,
                lastUsedAt INTEGER NOT NULL,
                usageCount INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_receipt_patterns_restaurantName ON receipt_patterns(restaurantName)"
        )
    }
}
