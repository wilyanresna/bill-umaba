package com.pndnwngi.billumaba.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressor @Inject constructor() {

    suspend fun compressImage(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        try {
            // 1. Get original dimensions first without loading full bitmap to memory (OOM protection)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return@withContext null

            // Calculate scale factor (max 1920px for width or height)
            val maxDim = 1920
            var sampleSize = 1
            if (srcWidth > maxDim || srcHeight > maxDim) {
                val halfWidth = srcWidth / 2
                val halfHeight = srcHeight / 2
                while ((halfWidth / sampleSize) >= maxDim || (halfHeight / sampleSize) >= maxDim) {
                    sampleSize *= 2
                }
            }

            // 2. Load the sampled/resized bitmap
            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, loadOptions)
            } ?: return@withContext null

            // 3. Resize bitmap precisely if it still exceeds 1920px in any dimension
            val currentWidth = sampledBitmap.width
            val currentHeight = sampledBitmap.height
            val finalBitmap = if (currentWidth > maxDim || currentHeight > maxDim) {
                val ratio = currentWidth.toFloat() / currentHeight.toFloat()
                val (newWidth, newHeight) = if (currentWidth > currentHeight) {
                    Pair(maxDim, (maxDim / ratio).toInt())
                } else {
                    Pair((maxDim * ratio).toInt(), maxDim)
                }
                val scaled = Bitmap.createScaledBitmap(sampledBitmap, newWidth, newHeight, true)
                if (scaled != sampledBitmap) {
                    sampledBitmap.recycle()
                }
                scaled
            } else {
                sampledBitmap
            }

            // 4. Iteratively compress to JPEG until size is < 500 KB (500 * 1024 bytes)
            val maxSizeBytes = 500 * 1024
            var quality = 80
            var byteArray: ByteArray? = null

            do {
                val outputStream = ByteArrayOutputStream()
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                byteArray = outputStream.toByteArray()
                quality -= 10
            } while (byteArray!!.size > maxSizeBytes && quality >= 10)

            finalBitmap.recycle()
            return@withContext byteArray
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
