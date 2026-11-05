package com.example.voidstep.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.voidstep.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = VoidBlack,
    primaryContainer = VoidSurfaceVariant,
    onPrimaryContainer = NeonCyanBright,
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = VoidSurface,
    onSecondaryContainer = NeonPurpleGlow,
    tertiary = ElectricBlue,
    onTertiary = Color.White,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = VoidDarkGraphite,
    onSurface = TextPrimary,
    surfaceVariant = VoidSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = VoidBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightPrimary,
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightSecondary,
    tertiary = ElectricBlue,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun VoidStepTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
