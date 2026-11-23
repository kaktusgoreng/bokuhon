package eu.kanade.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Four tones only: paper, mist, ash, ink. Anything that would normally carry an accent colour
// resolves to one of these.
internal object BokuhonPalette {
    val paper = Color(0xFFF7F7F7)
    val mist = Color(0xFFE3E3E3)
    val ash = Color(0xFF767676)
    val ink = Color(0xFF141414)
}

internal val BokuhonLightScheme: ColorScheme = lightColorScheme(
    primary = BokuhonPalette.ink,
    onPrimary = BokuhonPalette.paper,
    primaryContainer = BokuhonPalette.ink,
    onPrimaryContainer = BokuhonPalette.paper,
    inversePrimary = BokuhonPalette.paper,
    secondary = BokuhonPalette.ash,
    onSecondary = BokuhonPalette.paper,
    secondaryContainer = BokuhonPalette.mist,
    onSecondaryContainer = BokuhonPalette.ink,
    tertiary = BokuhonPalette.ash,
    onTertiary = BokuhonPalette.paper,
    tertiaryContainer = BokuhonPalette.mist,
    onTertiaryContainer = BokuhonPalette.ink,
    background = BokuhonPalette.paper,
    onBackground = BokuhonPalette.ink,
    surface = BokuhonPalette.paper,
    onSurface = BokuhonPalette.ink,
    surfaceVariant = BokuhonPalette.mist,
    onSurfaceVariant = BokuhonPalette.ink,
    surfaceTint = BokuhonPalette.ink,
    inverseSurface = BokuhonPalette.ink,
    inverseOnSurface = BokuhonPalette.paper,
    error = BokuhonPalette.ink,
    onError = BokuhonPalette.paper,
    errorContainer = BokuhonPalette.mist,
    onErrorContainer = BokuhonPalette.ink,
    outline = BokuhonPalette.ash,
    outlineVariant = BokuhonPalette.mist,
    scrim = BokuhonPalette.ink,
    surfaceBright = BokuhonPalette.paper,
    surfaceDim = BokuhonPalette.mist,
    surfaceContainerLowest = BokuhonPalette.paper,
    surfaceContainerLow = BokuhonPalette.paper,
    surfaceContainer = BokuhonPalette.paper,
    surfaceContainerHigh = BokuhonPalette.mist,
    surfaceContainerHighest = BokuhonPalette.mist,
)

internal val BokuhonDarkScheme: ColorScheme = darkColorScheme(
    primary = BokuhonPalette.paper,
    onPrimary = BokuhonPalette.ink,
    primaryContainer = BokuhonPalette.paper,
    onPrimaryContainer = BokuhonPalette.ink,
    inversePrimary = BokuhonPalette.ink,
    secondary = BokuhonPalette.ash,
    onSecondary = BokuhonPalette.paper,
    secondaryContainer = BokuhonPalette.ash,
    onSecondaryContainer = BokuhonPalette.paper,
    tertiary = BokuhonPalette.ash,
    onTertiary = BokuhonPalette.paper,
    tertiaryContainer = BokuhonPalette.ash,
    onTertiaryContainer = BokuhonPalette.paper,
    background = BokuhonPalette.ink,
    onBackground = BokuhonPalette.paper,
    surface = BokuhonPalette.ink,
    onSurface = BokuhonPalette.paper,
    surfaceVariant = BokuhonPalette.ash,
    onSurfaceVariant = BokuhonPalette.paper,
    surfaceTint = BokuhonPalette.paper,
    inverseSurface = BokuhonPalette.paper,
    inverseOnSurface = BokuhonPalette.ink,
    error = BokuhonPalette.paper,
    onError = BokuhonPalette.ink,
    errorContainer = BokuhonPalette.ash,
    onErrorContainer = BokuhonPalette.paper,
    outline = BokuhonPalette.ash,
    outlineVariant = BokuhonPalette.ash,
    scrim = BokuhonPalette.ink,
    surfaceBright = BokuhonPalette.ash,
    surfaceDim = BokuhonPalette.ink,
    surfaceContainerLowest = BokuhonPalette.ink,
    surfaceContainerLow = BokuhonPalette.ink,
    surfaceContainer = BokuhonPalette.ink,
    surfaceContainerHigh = BokuhonPalette.ash,
    surfaceContainerHighest = BokuhonPalette.ash,
)
