package com.pndnwngi.billumaba.data.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val receiptsDir: File
        get() = File(context.filesDir, "receipts").apply {
            if (!exists()) {
                mkdirs()
            }
        }

    suspend fun saveReceiptImage(byteArray: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "receipt_${UUID.randomUUID()}.jpg"
            val file = File(receiptsDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(byteArray)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteReceiptImage(absolutePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(absolutePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
