package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import tachiyomi.presentation.core.bokuhon.BokuhonPadding
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.Text

@Composable
fun ListGroupHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .padding(
                horizontal = BokuhonPadding.medium,
                vertical = BokuhonPadding.small,
            ),
        color = LocalBokuhonColors.current.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        style = LocalBokuhonTypography.current.bodyMedium,
    )
}
