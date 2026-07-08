package com.pndnwngi.billumaba.data.ocr

import android.graphics.Rect
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OcrResult(val lines: List<OcrLine>) : Parcelable

@Parcelize
data class OcrLine(
    val text: String,
    val boundingBox: Rect?,
    val confidence: Float?
) : Parcelable
