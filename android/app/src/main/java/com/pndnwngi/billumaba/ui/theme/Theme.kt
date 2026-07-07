package com.pndnwngi.billumaba.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkWarmPrimary,
    onPrimary = DarkWarmOnPrimary,
    primaryContainer = DarkWarmPrimaryContainer,
    onPrimaryContainer = DarkWarmOnPrimaryContainer,
    secondary = DarkWarmSecondary,
    onSecondary = DarkWarmOnSecondary,
    secondaryContainer = DarkWarmSecondaryContainer,
    onSecondaryContainer = DarkWarmOnSecondaryContainer,
    tertiary = DarkWarmTertiary,
    onTertiary = DarkWarmOnTertiary,
    tertiaryContainer = DarkWarmTertiaryContainer,
    onTertiaryContainer = DarkWarmOnTertiaryContainer,
    background = DarkWarmBackground,
    onBackground = DarkWarmOnBackground,
    surface = DarkWarmSurface,
    onSurface = DarkWarmOnSurface,
    surfaceVariant = DarkWarmSurfaceVariant,
    onSurfaceVariant = DarkWarmOnSurfaceVariant,
    error = DarkWarmError,
    onError = DarkWarmOnError,
    errorContainer = DarkWarmErrorContainer,
    onErrorContainer = DarkWarmOnErrorContainer,
    outline = DarkWarmOutline,
    outlineVariant = DarkWarmOutlineVariant,
    inverseSurface = DarkWarmInverseSurface,
    inverseOnSurface = DarkWarmInverseOnSurface,
    inversePrimary = DarkWarmInversePrimary,
    surfaceTint = DarkWarmSurfaceTint
)

private val LightColorScheme = lightColorScheme(
    primary = WarmPrimary,
    onPrimary = WarmOnPrimary,
    primaryContainer = WarmPrimaryContainer,
    onPrimaryContainer = WarmOnPrimaryContainer,
    secondary = WarmSecondary,
    onSecondary = WarmOnSecondary,
    secondaryContainer = WarmSecondaryContainer,
    onSecondaryContainer = WarmOnSecondaryContainer,
    tertiary = WarmTertiary,
    onTertiary = WarmOnTertiary,
    tertiaryContainer = WarmTertiaryContainer,
    onTertiaryContainer = WarmOnTertiaryContainer,
    background = WarmBackground,
    onBackground = WarmOnBackground,
    surface = WarmSurface,
    onSurface = WarmOnSurface,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmOnSurfaceVariant,
    error = WarmError,
    onError = WarmOnError,
    errorContainer = WarmErrorContainer,
    onErrorContainer = WarmOnErrorContainer,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant,
    inverseSurface = WarmInverseSurface,
    inverseOnSurface = WarmInverseOnSurface,
    inversePrimary = WarmInversePrimary,
    surfaceTint = WarmSurfaceTint
)

@Composable
fun BillUmabaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
