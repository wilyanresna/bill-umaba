package com.pndnwngi.billumaba.data.repository

import com.pndnwngi.billumaba.data.database.entities.MenuItemEntity
import com.pndnwngi.billumaba.data.database.entities.VisitEntity
import com.pndnwngi.billumaba.data.database.entities.VisitWithMenus
import kotlinx.coroutines.flow.Flow

interface CulinaryRepository {
    fun getAllVisitsWithMenus(): Flow<List<VisitWithMenus>>
    
    fun getVisitWithMenusById(id: Long): Flow<VisitWithMenus?>
    
    fun searchVisits(query: String): Flow<List<VisitWithMenus>>
    
    suspend fun saveVisit(
        visit: VisitEntity,
        menuItems: List<MenuItemEntity>,
        newPhotoUri: String? = null,
        deleteOldPhoto: Boolean = false
    ): Long
    
    suspend fun deleteVisit(visit: VisitEntity)
}
