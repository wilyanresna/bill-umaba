package com.pndnwngi.billumaba.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val restaurantName: String,
    val restaurantAddress: String?,
    val restaurantRating: Float,
    val restaurantReview: String?,
    val visitDate: Long,
    val receiptPhotoPath: String?,
    val grandTotal: Double
)
