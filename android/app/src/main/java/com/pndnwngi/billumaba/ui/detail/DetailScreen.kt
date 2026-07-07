package com.pndnwngi.billumaba.ui.detail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(text = "Detail Screen Placeholder")
}
