package com.pndnwngi.billumaba.ui.components

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.pndnwngi.billumaba.util.isGooglePlayServicesAvailable

@Composable
fun rememberReceiptScanner(
    onResult: (Uri?) -> Unit,
    onGmsUnavailable: () -> Unit
): () -> Unit {
    val context = LocalContext.current

    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setGalleryImportAllowed(false)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setPageLimit(1)
            .build()
    }

    val scanner = remember { GmsDocumentScanning.getClient(options) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            val pages = result?.getPages()
            if (pages != null && pages.isNotEmpty()) {
                onResult(pages[0].getImageUri())
            } else {
                onResult(null)
            }
        } else {
            onResult(null)
        }
    }

    return remember {
        {
            if (isGooglePlayServicesAvailable(context)) {
                val activity = context as? Activity
                if (activity == null) {
                    onGmsUnavailable()
                    return@remember
                }

                scanner.getStartScanIntent(activity)
                    .addOnSuccessListener { intentSender ->
                        val request = IntentSenderRequest.Builder(intentSender).build()
                        launcher.launch(request)
                    }
                    .addOnFailureListener {
                        onGmsUnavailable()
                    }
            } else {
                onGmsUnavailable()
            }
        }
    }
}
