package eu.kanade.presentation.browse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.bokuhon.BokuhonPadding
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.Text

@Composable
internal fun ExtensionPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalBokuhonColors.current.onSurfaceVariant,
) {
    Text(
        text = text,
        modifier = modifier
            .background(color = color.copy(alpha = PILL_BACKGROUND_ALPHA), shape = RectangleShape)
            .padding(horizontal = BokuhonPadding.small, vertical = 2.dp),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = LocalBokuhonTypography.current.labelSmall,
    )
}

private const val PILL_BACKGROUND_ALPHA = 0.12f
