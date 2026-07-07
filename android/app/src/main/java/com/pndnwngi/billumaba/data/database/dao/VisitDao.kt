package com.pndnwngi.billumaba.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pndnwngi.billumaba.data.database.entities.VisitEntity
import com.pndnwngi.billumaba.data.database.entities.VisitWithMenus
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visit: VisitEntity): Long

    @Update
    suspend fun update(visit: VisitEntity)

    @Delete
    suspend fun delete(visit: VisitEntity)

    @Transaction
    @Query("SELECT * FROM visits WHERE id = :id")
    fun getVisitById(id: Long): Flow<VisitWithMenus?>

    @Transaction
    @Query("SELECT * FROM visits ORDER BY visitDate DESC")
    fun getAllVisits(): Flow<List<VisitWithMenus>>

    @Transaction
    @Query("""
        SELECT DISTINCT visits.* FROM visits 
        LEFT JOIN menu_items ON visits.id = menu_items.visitId 
        WHERE :searchQuery = '' 
           OR visits.restaurantName LIKE '%' || :searchQuery || '%' 
           OR visits.restaurantAddress LIKE '%' || :searchQuery || '%' 
           OR menu_items.name LIKE '%' || :searchQuery || '%'
        ORDER BY visits.visitDate DESC
    """)
    fun searchVisits(searchQuery: String): Flow<List<VisitWithMenus>>
}
