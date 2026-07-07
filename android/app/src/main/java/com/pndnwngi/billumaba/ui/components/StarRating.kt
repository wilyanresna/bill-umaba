package com.pndnwngi.billumaba.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

@Composable
fun StarRatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 24.dp,
    starColor: Color = MaterialTheme.colorScheme.primary,
    emptyStarColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            val fillPercentage = when {
                rating >= i.toFloat() -> 1f
                rating >= i - 1 -> rating - (i - 1)
                else -> 0f
            }
            StarIcon(
                fillPercentage = fillPercentage,
                size = starSize,
                filledColor = starColor,
                emptyColor = emptyStarColor
            )
        }
    }
}

@Composable
fun StarRatingInput(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 40.dp,
    starColor: Color = MaterialTheme.colorScheme.primary,
    emptyStarColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val density = LocalDensity.current
    val starSizePx = with(density) { starSize.toPx() }
    val totalWidthPx = starSizePx * 5

    Box(
        modifier = modifier
            .size(width = starSize * 5, height = starSize)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newRating = calculateRating(offset.x, totalWidthPx)
                    onRatingChanged(newRating.coerceIn(1f, 5f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    val newRating = calculateRating(change.position.x, totalWidthPx)
                    onRatingChanged(newRating.coerceIn(1f, 5f))
                }
            }
    ) {
        Row {
            for (i in 1..5) {
                val fillPercentage = when {
                    rating >= i.toFloat() -> 1f
                    rating >= i - 1 -> rating - (i - 1)
                    else -> 0f
                }
                StarIcon(
                    fillPercentage = fillPercentage,
                    size = starSize,
                    filledColor = starColor,
                    emptyColor = emptyStarColor
                )
            }
        }
    }
}

private fun calculateRating(x: Float, totalWidth: Float): Float {
    val percentage = (x / totalWidth).coerceIn(0f, 1f)
    val rawRating = percentage * 5f
    val roundedRating = floor(rawRating * 2) / 2f
    return (roundedRating + 0.5f).coerceIn(1f, 5f)
}

@Composable
private fun StarIcon(
    fillPercentage: Float,
    size: Dp,
    filledColor: Color,
    emptyColor: Color
) {
    Canvas(modifier = Modifier.size(size)) {
        drawStar(
            fillPercentage = fillPercentage,
            filledColor = filledColor,
            emptyColor = emptyColor
        )
    }
}

private fun DrawScope.drawStar(
    fillPercentage: Float,
    filledColor: Color,
    emptyColor: Color
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val outerRadius = size.minDimension / 2
    val innerRadius = outerRadius * 0.38f

    val starPath = createStarPath(centerX, centerY, outerRadius, innerRadius)

    drawPath(
        path = starPath,
        color = emptyColor,
        style = Fill
    )

    if (fillPercentage > 0f) {
        val clipPath = Path().apply {
            addRect(
                androidx.compose.ui.geometry.Rect(
                    left = 0f,
                    top = 0f,
                    right = size.width * fillPercentage,
                    bottom = size.height
                )
            )
        }

        drawPath(
            path = starPath,
            color = filledColor,
            style = Fill
        )
    }
}

private fun createStarPath(
    centerX: Float,
    centerY: Float,
    outerRadius: Float,
    innerRadius: Float
): Path {
    val points = 5
    val startAngle = -Math.PI / 2

    return Path().apply {
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = startAngle + (i * Math.PI / points)
            val x = centerX + (radius * Math.cos(angle)).toFloat()
            val y = centerY + (radius * Math.sin(angle)).toFloat()

            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
    }
}
