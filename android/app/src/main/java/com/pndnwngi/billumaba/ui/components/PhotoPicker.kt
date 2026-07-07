package com.pndnwngi.billumaba.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun PhotoPicker(
    selectedPhotoPath: String?,
    onPhotoSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    
    // Load local bitmap dynamically from selectedPhotoPath
    var bitmap by remember(selectedPhotoPath) { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(selectedPhotoPath) {
        if (!selectedPhotoPath.isNullOrBlank()) {
            try {
                val uri = if (selectedPhotoPath.startsWith("content://") || selectedPhotoPath.startsWith("file://")) {
                    Uri.parse(selectedPhotoPath)
                } else {
                    Uri.fromFile(File(selectedPhotoPath))
                }
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    bitmap = BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                bitmap = null
            }
        } else {
            bitmap = null
        }
    }

    // Keep track of the temporary camera image URI
    var tempCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPhotoSelected(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { uri ->
                onPhotoSelected(uri)
            }
        }
    }

    fun launchCamera() {
        try {
            val tempFile = File.createTempFile("receipt_capture_", ".jpg", context.cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Foto Struk Belanja",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay to prompt change
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ketuk untuk Mengubah Foto",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tambah Foto Struk",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Pilih Sumber Foto") },
            text = {
                Text(text = "Ambil foto struk belanja baru dengan Kamera atau pilih dari Galeri.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        launchCamera()
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Kamera")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Galeri")
                    }
                }
            }
        )
    }
}
