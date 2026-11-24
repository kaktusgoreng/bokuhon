package eu.kanade.presentation.reader

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Refresh
import tachiyomi.presentation.core.bokuhon.Icon
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors

/**
 * A still glyph for a page whose image is not up yet: a turning ring is a grey smear on e-ink.
 */
@Composable
fun ReaderLoadingIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = MaterialSymbols.Rounded.Refresh,
        contentDescription = null,
        tint = LocalBokuhonColors.current.onBackground,
        modifier = modifier.size(40.dp),
    )
}
