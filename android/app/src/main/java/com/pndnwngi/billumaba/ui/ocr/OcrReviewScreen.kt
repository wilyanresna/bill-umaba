package com.pndnwngi.billumaba.ui.ocr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pndnwngi.billumaba.data.ocr.OcrResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrReviewScreen(
    ocrResult: OcrResult?,
    onNavigateBack: () -> Unit,
    viewModel: OcrReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(ocrResult) {
        viewModel.loadOcrResult(ocrResult)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Hasil OCR") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Memproses OCR...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = "Baris Teks (${uiState.editedLines.size})",
                style = MaterialTheme.typography.titleSmall
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.editedLines) { index, line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(32.dp)
                        )

                        OutlinedTextField(
                            value = line,
                            onValueChange = { newText -> viewModel.updateLineText(index, newText) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        val lineData = uiState.ocrResult?.lines?.getOrNull(index)
                        if (lineData?.confidence != null) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "${(lineData.confidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = when {
                                        lineData.confidence >= 0.9f -> MaterialTheme.colorScheme.primaryContainer
                                        lineData.confidence >= 0.7f -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.errorContainer
                                    }
                                )
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.applyToForm(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.editedLines.isNotEmpty()
            ) {
                Text(text = "Pakai Hasil Scan")
            }
        }
    }
}
