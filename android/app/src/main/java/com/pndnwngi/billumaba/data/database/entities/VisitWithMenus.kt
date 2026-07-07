package com.pndnwngi.billumaba.data.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class VisitWithMenus(
    @Embedded
    val visit: VisitEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "visitId"
    )
    val menuItems: List<MenuItemEntity>
)
