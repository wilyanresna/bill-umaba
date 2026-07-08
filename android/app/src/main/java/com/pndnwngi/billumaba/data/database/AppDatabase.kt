package com.pndnwngi.billumaba.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pndnwngi.billumaba.data.database.dao.MenuDao
import com.pndnwngi.billumaba.data.database.dao.ReceiptPatternDao
import com.pndnwngi.billumaba.data.database.dao.VisitDao
import com.pndnwngi.billumaba.data.database.entities.MenuItemEntity
import com.pndnwngi.billumaba.data.database.entities.ReceiptPatternEntity
import com.pndnwngi.billumaba.data.database.entities.VisitEntity

@Database(
    entities = [VisitEntity::class, MenuItemEntity::class, ReceiptPatternEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun visitDao(): VisitDao
    abstract fun menuDao(): MenuDao
    abstract fun receiptPatternDao(): ReceiptPatternDao
}
