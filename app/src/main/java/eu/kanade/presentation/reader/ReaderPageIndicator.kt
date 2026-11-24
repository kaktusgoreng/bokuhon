package eu.kanade.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import tachiyomi.presentation.core.bokuhon.BokuhonPadding
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.Surface
import tachiyomi.presentation.core.bokuhon.Text

@Composable
fun ReaderPageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    if (currentPage <= 0 || totalPages <= 0) return

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
            style = LocalBokuhonTypography.current.bodySmall,
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
