package eu.kanade.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import tachiyomi.presentation.core.bokuhon.BokuhonDialogHost
import tachiyomi.presentation.core.bokuhon.ProvideBokuhonDialogHost
import tachiyomi.presentation.core.bokuhon.ProvideBokuhonTheme

@Composable
fun TachiyomiTheme(content: @Composable () -> Unit) {
    // The activity is pinned to light, so only the reader's dark background contexts reach the
    // dark scheme.
    val isDark = isSystemInDarkTheme()
    val dialogHost = remember { BokuhonDialogHost() }
    ProvideBokuhonTheme(isDark = isDark) {
        ProvideBokuhonDialogHost(host = dialogHost, content = content)
    }
}

@Composable
fun TachiyomiPreviewTheme(content: @Composable () -> Unit) = TachiyomiTheme(content)
