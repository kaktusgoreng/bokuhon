package eu.kanade.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import tachiyomi.presentation.core.bokuhon.ProvideBokuhonTheme

@Composable
fun TachiyomiTheme(content: @Composable () -> Unit) {
    // The activity is pinned to light, so only the reader's dark background contexts reach the
    // dark scheme.
    val isDark = isSystemInDarkTheme()
    CompositionLocalProvider(
        // Touch ripples are motion too; e-ink panels smear them into grey trails.
        LocalRippleConfiguration provides null,
    ) {
        ProvideBokuhonTheme(isDark = isDark) {
            MaterialTheme(
                colorScheme = remember(isDark) {
                    if (isDark) BokuhonDarkScheme else BokuhonLightScheme
                },
                shapes = SquareShapes,
                content = content,
            )
        }
    }
}

@Composable
fun TachiyomiPreviewTheme(content: @Composable () -> Unit) = TachiyomiTheme(content)
