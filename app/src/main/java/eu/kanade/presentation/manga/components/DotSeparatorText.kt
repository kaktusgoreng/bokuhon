package eu.kanade.presentation.manga.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tachiyomi.presentation.core.bokuhon.Text

@Composable
fun DotSeparatorText(
    modifier: Modifier = Modifier,
) {
    Text(
        text = " • ",
        modifier = modifier,
    )
}
