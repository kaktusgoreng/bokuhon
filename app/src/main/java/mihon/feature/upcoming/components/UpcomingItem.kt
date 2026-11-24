package mihon.feature.upcoming.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.manga.components.MangaCover
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.asMangaCover
import tachiyomi.presentation.core.bokuhon.BokuhonPadding
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.Text

private val UpcomingItemHeight = 96.dp

@Composable
fun UpcomingItem(
    upcoming: Manga,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(UpcomingItemHeight)
            .padding(
                horizontal = BokuhonPadding.medium,
                vertical = BokuhonPadding.small,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BokuhonPadding.large),
    ) {
        MangaCover.Book(
            modifier = Modifier.fillMaxHeight(),
            data = upcoming.asMangaCover(),
        )
        Text(
            modifier = Modifier.weight(1f),
            text = upcoming.title,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = LocalBokuhonTypography.current.bodyMedium,
        )
    }
}
