package com.pndnwngi.billumaba

import android.content.Context
import android.net.Uri
import com.pndnwngi.billumaba.data.database.AppDatabase
import com.pndnwngi.billumaba.data.database.dao.MenuDao
import com.pndnwngi.billumaba.data.database.dao.VisitDao
import com.pndnwngi.billumaba.data.database.entities.MenuItemEntity
import com.pndnwngi.billumaba.data.database.entities.VisitEntity
import com.pndnwngi.billumaba.data.repository.CulinaryRepositoryImpl
import com.pndnwngi.billumaba.data.storage.ImageCompressor
import com.pndnwngi.billumaba.data.storage.StorageManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CulinaryRepositoryTest {

    private val context: Context = mock()
    private val appDatabase: AppDatabase = mock()
    private val visitDao: VisitDao = mock()
    private val menuDao: MenuDao = mock()
    private val storageManager: StorageManager = mock()
    private val imageCompressor: ImageCompressor = mock()

    private lateinit var repository: CulinaryRepositoryImpl

    @Before
    fun setUp() {
        // Mock database transaction behavior to run immediately
        whenever(appDatabase.runInTransaction(any<Runnable>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Runnable).run()
        }
        repository = CulinaryRepositoryImpl(
            context,
            appDatabase,
            visitDao,
            menuDao,
            storageManager,
            imageCompressor
        )
    }

    @Test
    fun deleteVisit_deletesImageFromStorage_andDeletesFromDatabase() = runTest {
        val visit = VisitEntity(
            id = 1L,
            restaurantName = "Warung Bu Rudy",
            restaurantAddress = "Jl. Dharmahusada",
            restaurantRating = 4.5f,
            restaurantReview = "Enak banget sambalnya!",
            visitDate = 1688730315000L,
            receiptPhotoPath = "/data/user/0/com.pndnwngi.billumaba/files/receipts/receipt_test.jpg",
            grandTotal = 150000.0
        )

        repository.deleteVisit(visit)

        // Verify that the photo file is deleted via storage manager
        verify(storageManager).deleteReceiptImage(visit.receiptPhotoPath!!)
        
        // Verify that the visit is deleted from the Room database
        verify(visitDao).delete(visit)
    }

    @Test
    fun deleteVisit_withNullPhotoPath_onlyDeletesFromDatabase() = runTest {
        val visit = VisitEntity(
            id = 2L,
            restaurantName = "Soto Ayam Ambengan",
            restaurantAddress = "Jl. Ambengan",
            restaurantRating = 4.7f,
            restaurantReview = "Kuah kental mantap.",
            visitDate = 1688730315000L,
            receiptPhotoPath = null,
            grandTotal = 75000.0
        )

        repository.deleteVisit(visit)

        // Verify that deleteReceiptImage is NEVER called
        verify(storageManager, never()).deleteReceiptImage(any())
        
        // Verify that the visit is deleted from the Room database
        verify(visitDao).delete(visit)
    }
}
