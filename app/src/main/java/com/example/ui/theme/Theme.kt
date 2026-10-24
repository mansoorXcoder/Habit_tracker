package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoDarkGray,
    onPrimaryContainer = MonoWhite,
    secondary = MonoGrayLight,
    onSecondary = MonoBlack,
    background = HabitBackgroundDark,
    onBackground = HabitTextPrimaryDark,
    surface = HabitSurfaceDark,
    onSurface = HabitTextPrimaryDark,
    surfaceVariant = HabitSurfaceVariantDark,
    onSurfaceVariant = HabitTextSecondaryDark,
    outline = Color(0xFF3F3F46),
    outlineVariant = Color(0xFF27272A)
)

private val LightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = MonoSurfaceVariant,
    onPrimaryContainer = MonoBlack,
    secondary = MonoDarkGray,
    onSecondary = MonoWhite,
    background = HabitBackgroundLight,
    onBackground = HabitTextPrimary,
    surface = HabitSurfaceLight,
    onSurface = HabitTextPrimary,
    surfaceVariant = HabitSurfaceVariant,
    onSurfaceVariant = HabitTextSecondary,
    outline = HabitOutline,
    outlineVariant = HabitOutlineVariant
)

@Composable
fun HabitFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep false to enforce monochrome consistency
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = HabitFlowTheme(darkTheme, dynamicColor, content)
