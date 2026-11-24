package eu.kanade.tachiyomi.ui.reader.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.kanade.presentation.reader.ChapterTransition
import eu.kanade.presentation.reader.reportPress
import eu.kanade.tachiyomi.ui.reader.model.ChapterTransition
import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.bokuhon.Button
import tachiyomi.presentation.core.bokuhon.CircularProgressIndicator
import tachiyomi.presentation.core.bokuhon.LocalBokuhonColors
import tachiyomi.presentation.core.bokuhon.LocalBokuhonContentColor
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTextStyle
import tachiyomi.presentation.core.bokuhon.LocalBokuhonTypography
import tachiyomi.presentation.core.bokuhon.Text
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ReaderTransitionPage(
    transition: ChapterTransition,
    downloadState: TransitionDownloadState?,
    onRetry: (ReaderChapter) -> Unit,
    onButtonPressChange: (pressed: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    statusPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (downloadState != null) {
            CompositionLocalProvider(
                LocalBokuhonTextStyle provides LocalBokuhonTypography.current.bodySmall,
                LocalBokuhonContentColor provides LocalBokuhonColors.current.onBackground,
            ) {
                ChapterTransition(
                    transition,
                    downloadState.currChapterDownloaded,
                    downloadState.goingToChapterDownloaded,
                )
            }
        }

        val toChapter = transition.to ?: return@Column
        val state by toChapter.stateFlow.collectAsState()
        when (val state = state) {
            is ReaderChapter.State.Loading -> Column(
                modifier = Modifier.padding(statusPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(MR.strings.transition_pages_loading),
                    color = LocalBokuhonColors.current.onBackground,
                    style = LocalBokuhonTypography.current.bodyMedium,
                )
            }
            is ReaderChapter.State.Error -> Column(
                modifier = Modifier.padding(statusPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(MR.strings.transition_pages_error, state.error.message ?: ""),
                    color = LocalBokuhonColors.current.onBackground,
                    style = LocalBokuhonTypography.current.bodyMedium,
                )
                Button(
                    onClick = { onRetry(toChapter) },
                    modifier = Modifier.reportPress(onButtonPressChange),
                ) {
                    Text(stringResource(MR.strings.action_retry))
                }
            }
            is ReaderChapter.State.Wait, is ReaderChapter.State.Loaded -> {}
        }
    }
}
