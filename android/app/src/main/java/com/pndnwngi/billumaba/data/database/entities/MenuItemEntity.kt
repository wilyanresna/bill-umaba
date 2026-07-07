package com.pndnwngi.billumaba.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = VisitEntity::class,
            parentColumns = ["id"],
            childColumns = ["visitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["visitId"])]
)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val visitId: Long,
    val name: String,
    val quantity: Int,
    val price: Double,
    val rating: Float,
    val notes: String?
)
