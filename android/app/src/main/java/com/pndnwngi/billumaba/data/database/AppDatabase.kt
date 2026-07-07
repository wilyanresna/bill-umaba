package com.pndnwngi.billumaba.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pndnwngi.billumaba.data.database.dao.MenuDao
import com.pndnwngi.billumaba.data.database.dao.VisitDao
import com.pndnwngi.billumaba.data.database.entities.MenuItemEntity
import com.pndnwngi.billumaba.data.database.entities.VisitEntity

@Database(
    entities = [VisitEntity::class, MenuItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun visitDao(): VisitDao
    abstract fun menuDao(): MenuDao
}
