package com.pndnwngi.billumaba.ui.dashboard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DashboardScreen(
    onNavigateToAddEdit: (Long?) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(text = "Dashboard Screen Placeholder")
}
