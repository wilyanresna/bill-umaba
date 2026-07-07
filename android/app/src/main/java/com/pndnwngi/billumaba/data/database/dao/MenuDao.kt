package com.pndnwngi.billumaba.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pndnwngi.billumaba.data.database.entities.MenuItemEntity

@Dao
interface MenuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(menuItem: MenuItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(menuItems: List<MenuItemEntity>)

    @Update
    suspend fun update(menuItem: MenuItemEntity)

    @Delete
    suspend fun delete(menuItem: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE visitId = :visitId")
    suspend fun deleteByVisitId(visitId: Long)

    @Query("SELECT * FROM menu_items WHERE visitId = :visitId")
    suspend fun getMenuItemsByVisitId(visitId: Long): List<MenuItemEntity>
}
