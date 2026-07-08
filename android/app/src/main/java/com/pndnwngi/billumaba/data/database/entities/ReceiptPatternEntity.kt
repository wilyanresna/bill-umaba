package com.pndnwngi.billumaba.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipt_patterns",
    indices = [Index(value = ["restaurantName"], unique = true)]
)
data class ReceiptPatternEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantName: String,
    val displayName: String,
    val menuLineTemplate: String,
    val totalLineStrategy: String,
    val totalLineRegex: String? = null,
    val taxLineRegex: String? = null,
    val serviceLineRegex: String? = null,
    val discountLineRegex: String? = null,
    val dateRegex: String? = null,
    val restaurantNameStrategy: String,
    val headerLineCount: Int = 2,
    val skipKeywords: String = "",
    val parserType: String = "GENERAL",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(),
    val usageCount: Int = 0
)
