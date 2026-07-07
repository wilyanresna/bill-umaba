package com.pndnwngi.billumaba.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.pndnwngi.billumaba.data.database.AppDatabase
import com.pndnwngi.billumaba.data.database.dao.MenuDao
import com.pndnwngi.billumaba.data.database.dao.VisitDao
import com.pndnwngi.billumaba.data.database.entities.MenuItemEntity
import com.pndnwngi.billumaba.data.database.entities.VisitEntity
import com.pndnwngi.billumaba.data.database.entities.VisitWithMenus
import com.pndnwngi.billumaba.data.storage.ImageCompressor
import com.pndnwngi.billumaba.data.storage.StorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CulinaryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val visitDao: VisitDao,
    private val menuDao: MenuDao,
    private val storageManager: StorageManager,
    private val imageCompressor: ImageCompressor
) : CulinaryRepository {

    override fun getAllVisitsWithMenus(): Flow<List<VisitWithMenus>> {
        return visitDao.getAllVisits()
    }

    override fun getVisitWithMenusById(id: Long): Flow<VisitWithMenus?> {
        return visitDao.getVisitById(id)
    }

    override fun searchVisits(query: String): Flow<List<VisitWithMenus>> {
        return visitDao.searchVisits(query)
    }

    override suspend fun saveVisit(
        visit: VisitEntity,
        menuItems: List<MenuItemEntity>,
        newPhotoUri: String?,
        deleteOldPhoto: Boolean
    ): Long = withContext(Dispatchers.IO) {
        var finalPhotoPath = visit.receiptPhotoPath

        // 1. Handle image deletion if requested
        if (deleteOldPhoto && !visit.receiptPhotoPath.isNullOrEmpty()) {
            storageManager.deleteReceiptImage(visit.receiptPhotoPath)
            finalPhotoPath = null
        }

        // 2. Handle new image compression and saving
        if (!newPhotoUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(newPhotoUri)
                val compressedBytes = imageCompressor.compressImage(context, uri)
                if (compressedBytes != null) {
                    // If we have an existing photo and we are replacing it, delete the old one first
                    if (!visit.receiptPhotoPath.isNullOrEmpty()) {
                        storageManager.deleteReceiptImage(visit.receiptPhotoPath)
                    }
                    finalPhotoPath = storageManager.saveReceiptImage(compressedBytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Save to database in a single transaction
        val visitToSave = visit.copy(receiptPhotoPath = finalPhotoPath)

        appDatabase.withTransaction {
            val visitId = if (visitToSave.id == 0L) {
                visitDao.insert(visitToSave)
            } else {
                visitDao.update(visitToSave)
                // Clear old menu items before inserting updated ones
                menuDao.deleteByVisitId(visitToSave.id)
                visitToSave.id
            }

            // Insert menu items with the actual visitId
            val menuItemsToInsert = menuItems.map { it.copy(id = 0L, visitId = visitId) }
            menuDao.insertAll(menuItemsToInsert)

            visitId
        }
    }

    override suspend fun deleteVisit(visit: VisitEntity) = withContext(Dispatchers.IO) {
        // Delete receipt photo if exists
        if (!visit.receiptPhotoPath.isNullOrEmpty()) {
            storageManager.deleteReceiptImage(visit.receiptPhotoPath)
        }
        
        // Delete from database (foreign key Cascade deletes menu items)
        visitDao.delete(visit)
    }
}
