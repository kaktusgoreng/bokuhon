package eu.kanade.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import tachiyomi.presentation.core.bokuhon.ProvideBokuhonTheme

@Composable
fun TachiyomiTheme(content: @Composable () -> Unit) {
    // The activity is pinned to light, so only the reader's dark background contexts reach the
    // dark scheme.
    val isDark = isSystemInDarkTheme()
    ProvideBokuhonTheme(isDark = isDark, content = content)
}

@Composable
fun TachiyomiPreviewTheme(content: @Composable () -> Unit) = TachiyomiTheme(content)
