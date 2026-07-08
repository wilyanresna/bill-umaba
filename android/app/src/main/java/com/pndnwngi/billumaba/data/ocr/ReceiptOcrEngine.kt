package com.pndnwngi.billumaba.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptOcrEngine @Inject constructor() {
    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.Builder().build()
    )

    suspend fun recognize(context: Context, imageUri: Uri): OcrResult =
        withContext(Dispatchers.Default) {
            val input = InputImage.fromFilePath(context, imageUri)
            val visionText = recognizer.process(input).await()
            OcrResult(
                lines = visionText.textBlocks.flatMap { block ->
                    block.lines.map { line ->
                        OcrLine(
                            text = line.text,
                            boundingBox = line.boundingBox,
                            confidence = line.confidence
                        )
                    }
                }
            )
        }
}
