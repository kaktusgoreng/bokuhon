package eu.kanade.presentation.manga.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichText
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography

@Composable
fun MangaNotesDisplay(
    content: String,
    modifier: Modifier,
) {
    val richTextState = rememberRichTextState()
    val primaryColor = LocalBokuhonColors.current.primary
    LaunchedEffect(content) {
        richTextState.setMarkdown(content)
    }
    LaunchedEffect(Unit) {
        richTextState.config.unorderedListIndent = 4
        richTextState.config.orderedListIndent = 20
    }
    LaunchedEffect(primaryColor) {
        richTextState.config.linkColor = primaryColor
    }

    SelectionContainer {
        BasicRichText(
            modifier = modifier,
            style = LocalBokuhonTypography.current.bodyMedium,
            state = richTextState,
        )
    }
}
