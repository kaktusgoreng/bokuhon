package eu.kanade.presentation.manga.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.bokuhon.BokuhonPadding
import tachiyomi.presentation.core.bokuhon.HorizontalDivider
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.Surface
import tachiyomi.presentation.core.bokuhon.Text
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.util.secondaryItemAlpha

@Composable
fun MissingChapterCountListItem(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(
                horizontal = BokuhonPadding.medium,
                vertical = BokuhonPadding.small,
            )
            .secondaryItemAlpha(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BokuhonPadding.medium),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = pluralStringResource(MR.plurals.missing_chapters, count = count, count),
            style = LocalBokuhonTypography.current.labelMedium,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    TachiyomiPreviewTheme {
        Surface {
            MissingChapterCountListItem(count = 42)
        }
    }
}
