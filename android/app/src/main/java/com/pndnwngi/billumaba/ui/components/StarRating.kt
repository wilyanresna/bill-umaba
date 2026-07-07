package com.pndnwngi.billumaba.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StarRating(
    rating: Float,
    onRatingChange: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    starSize: Dp = 24.dp,
    maxStars: Int = 5,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
) {
    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            val starRating = i.toFloat()
            val fraction = (rating - (i - 1)).coerceIn(0f, 1f)
            
            Box(
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRatingChange != null) {
                            Modifier.clickable {
                                onRatingChange(starRating)
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                // Inactive star (background)
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = inactiveColor,
                    modifier = Modifier.size(starSize)
                )
                
                // Active star (foreground with fractional width clipping)
                if (fraction > 0f) {
                    Box(
                        modifier = Modifier
                            .size(width = starSize * fraction, height = starSize)
                            .clipToBounds()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(starSize)
                        )
                    }
                }
            }
        }
    }
}
