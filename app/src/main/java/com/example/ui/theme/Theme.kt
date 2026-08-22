package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NaturalSageLight,
    onPrimary = Color(0xFF0D251A),
    primaryContainer = NaturalSageDarkContainer,
    onPrimaryContainer = Color(0xFFD4F3DE),
    secondary = NaturalMineralLight,
    onSecondary = Color(0xFF0C2430),
    secondaryContainer = NaturalBlueDarkContainer,
    onSecondaryContainer = Color(0xFFD1ECFA),
    tertiary = NaturalSandLight,
    onTertiary = Color(0xFF282012),
    tertiaryContainer = NaturalSandDarkContainer,
    onTertiaryContainer = Color(0xFFF7ECD3),
    background = NaturalDarkBackground,
    onBackground = Color(0xFFE4EDE6),
    surface = NaturalDarkSurface,
    onSurface = Color(0xFFE4EDE6),
    surfaceVariant = NaturalDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBDCCC0),
    outline = NaturalDarkOutline,
    outlineVariant = NaturalDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = NaturalForestPrimary,
    onPrimary = Color.White,
    primaryContainer = NaturalSageContainer, // Soft Sage #DEE9D1
    onPrimaryContainer = NaturalForestDark, // #2D3A30
    secondary = NaturalMineralPrimary,
    onSecondary = Color.White,
    secondaryContainer = NaturalBlueContainer, // Soft Mineral Blue #D1E1E9
    onSecondaryContainer = Color(0xFF142936),
    tertiary = NaturalSandPrimary,
    onTertiary = Color.White,
    tertiaryContainer = NaturalSandContainer, // Soft Sand #E9E1D1
    onTertiaryContainer = Color(0xFF2B2213),
    background = NaturalLightBackground, // #F5F8F6
    onBackground = NaturalForestDark, // #2D3A30
    surface = NaturalLightSurface,
    onSurface = NaturalForestDark, // #2D3A30
    surfaceVariant = NaturalLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF45554A),
    outline = NaturalLightOutline,
    outlineVariant = NaturalLightOutlineVariant
)

@Composable
fun JournalMeditationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

