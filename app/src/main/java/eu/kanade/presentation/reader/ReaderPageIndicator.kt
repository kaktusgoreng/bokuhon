package eu.kanade.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import kotlinx.coroutines.delay
import tachiyomi.presentation.core.bokuhon.BokuhonPadding
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.Surface
import tachiyomi.presentation.core.bokuhon.Text

/** How long the page number stays up after a page change. */
private const val VISIBLE_MILLIS = 5_000L

@Composable
fun ReaderPageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    if (currentPage <= 0 || totalPages <= 0) return

    // Shows on each page change, then clears itself; removing it beats fading it out on e-ink.
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(currentPage, totalPages) {
        visible = true
        delay(VISIBLE_MILLIS)
        visible = false
    }
    if (!visible) return

    val colors = LocalBokuhonColors.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            // A gap off the panel edge, where e-ink devices clip into their bezel.
            .padding(bottom = BokuhonPadding.small)
            .background(colors.surface)
            // Ink in the light theme, so the block reads against a white page.
            .border(1.dp, colors.onSurface)
            .padding(horizontal = BokuhonPadding.small, vertical = 2.dp),
    ) {
        Text(
            text = "$currentPage / $totalPages",
            color = colors.onSurface,
            // Page numbers read left to right even in an RTL language.
            style = LocalBokuhonTypography.current.bodySmall.copy(textDirection = TextDirection.Ltr),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

@PreviewLightDark
@Composable
private fun ReaderPageIndicatorPreview() {
    TachiyomiPreviewTheme {
        Surface {
            ReaderPageIndicator(currentPage = 10, totalPages = 69)
        }
    }
}
